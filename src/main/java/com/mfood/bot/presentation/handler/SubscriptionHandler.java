package com.mfood.bot.presentation.handler;

import com.mfood.bot.application.service.MessageService;
import com.mfood.bot.application.service.SubscriptionService;
import com.mfood.bot.domain.model.Subscription;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.infrastructure.config.ClickProperties;
import com.mfood.bot.infrastructure.payment.ClickPaymentService;
import com.mfood.bot.presentation.keyboard.InlineKeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionHandler {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final SubscriptionService subscriptionService;
    private final MessageService messageService;
    private final InlineKeyboardFactory inlineKeyboardFactory;
    private final ClickPaymentService clickPaymentService;
    private final ClickProperties clickProperties;

    public SendMessage handleSubscription(Update update, User user) {
        String chatId = update.getMessage().getChatId().toString();
        Optional<Subscription> sub = subscriptionService.getSubscription(user.getTelegramId());

        StringBuilder sb = new StringBuilder();
        sb.append(messageService.getMessage("subscription.title", user.getLanguage())).append("\n\n");

        if (sub.isPresent() && subscriptionService.isSubscriptionActive(user.getTelegramId())) {
            String expiresAt = sub.get().getExpiresAt().format(DATE_FMT);
            sb.append(messageService.getMessage("subscription.active", user.getLanguage(), expiresAt));
            return SendMessage.builder().chatId(chatId).text(sb.toString()).build();
        } else if (sub.isPresent()) {
            sb.append(messageService.getMessage("subscription.expired", user.getLanguage()));
        } else {
            sb.append(messageService.getMessage("subscription.inactive", user.getLanguage()));
        }

        return SendMessage.builder()
                .chatId(chatId)
                .text(sb.toString())
                .replyMarkup(inlineKeyboardFactory.buildSubscriptionKeyboard(user.getLanguage()))
                .build();
    }

    public SendInvoice handleSubscribePay(CallbackQuery callback, User user) {
        String payload = clickPaymentService.generateInvoicePayload(user.getTelegramId(), "MONTHLY");
        return SendInvoice.builder()
                .chatId(callback.getMessage().getChatId())
                .title(clickPaymentService.buildInvoiceTitle(user.getLanguage()))
                .description(clickPaymentService.buildInvoiceDescription(user.getLanguage()))
                .payload(payload)
                .providerToken(clickProperties.getProviderToken())
                .currency("UZS")
                .prices(List.of(new LabeledPrice("MFood Monthly", 1300000))) // 13000 UZS in tiyin
                .build();
    }

    public AnswerPreCheckoutQuery handlePreCheckout(Update update, User user) {
        String queryId = update.getPreCheckoutQuery().getId();
        log.info("Pre-checkout query id={} for telegramId={}", queryId, user.getTelegramId());
        // Always approve - validation can be added here
        return AnswerPreCheckoutQuery.builder()
                .preCheckoutQueryId(queryId)
                .ok(true)
                .build();
    }

    public SendMessage handleSuccessfulPayment(Update update, User user) {
        SuccessfulPayment payment = update.getMessage().getSuccessfulPayment();
        log.info("Successful payment for telegramId={}, chargeId={}", user.getTelegramId(),
                payment.getTelegramPaymentChargeId());

        subscriptionService.processSuccessfulPayment(
                user.getTelegramId(),
                payment.getTelegramPaymentChargeId(),
                payment.getProviderPaymentChargeId(),
                payment.getTotalAmount()
        );

        Optional<Subscription> sub = subscriptionService.getSubscription(user.getTelegramId());
        String expiresAt = sub.map(s -> s.getExpiresAt().format(DATE_FMT)).orElse("N/A");

        return SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text(messageService.getMessage("subscription.success", user.getLanguage(), expiresAt))
                .build();
    }
}
