package com.mfood.bot.infrastructure.scheduler;

import com.mfood.bot.application.service.MessageService;
import com.mfood.bot.application.service.SubscriptionService;
import com.mfood.bot.domain.model.Subscription;
import com.mfood.bot.presentation.bot.MFoodBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionRenewalScheduler {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final SubscriptionService subscriptionService;
    private final MessageService messageService;
    private final MFoodBot bot;

    @Value("${subscription.renewal-reminder-days:7}")
    private int renewalReminderDays;

    @Scheduled(cron = "0 0 9 * * *")
    public void checkExpiringSubscriptions() {
        List<Subscription> expiring = subscriptionService.getExpiringSubscriptions(renewalReminderDays);
        log.info("Found {} subscriptions expiring within {} days", expiring.size(), renewalReminderDays);

        for (Subscription sub : expiring) {
            try {
                var user = sub.getUser();
                String message = messageService.getMessage("subscription.renewal_reminder",
                        user.getLanguage(), renewalReminderDays);
                bot.execute(SendMessage.builder()
                        .chatId(user.getTelegramId().toString())
                        .text(message)
                        .build());
            } catch (Exception e) {
                log.error("Failed to send renewal reminder for subscription id={}: {}", sub.getId(), e.getMessage());
            }
        }
    }
}
