package com.mfood.bot.presentation.bot;

import com.mfood.bot.application.service.MessageService;
import com.mfood.bot.application.service.UserService;
import com.mfood.bot.domain.enums.Language;
import com.mfood.bot.domain.enums.UserState;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.presentation.handler.*;
import com.mfood.bot.presentation.keyboard.MainMenuKeyboard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
@Component
public class TelegramUpdateRouter {

    private final UserService userService;
    private final MessageService messageService;
    private final StartHandler startHandler;
    private final OnboardingHandler onboardingHandler;
    private final MealHandler mealHandler;
    private final HistoryHandler historyHandler;
    private final TargetHandler targetHandler;
    private final ProgressHandler progressHandler;
    private final ReminderHandler reminderHandler;
    private final SubscriptionHandler subscriptionHandler;
    private final SettingsHandler settingsHandler;
    private final MainMenuKeyboard mainMenuKeyboard;

    public TelegramUpdateRouter(
            UserService userService, MessageService messageService,
            StartHandler startHandler, OnboardingHandler onboardingHandler,
            MealHandler mealHandler, HistoryHandler historyHandler,
            TargetHandler targetHandler, ProgressHandler progressHandler,
            ReminderHandler reminderHandler, SubscriptionHandler subscriptionHandler,
            SettingsHandler settingsHandler, MainMenuKeyboard mainMenuKeyboard) {
        this.userService = userService;
        this.messageService = messageService;
        this.startHandler = startHandler;
        this.onboardingHandler = onboardingHandler;
        this.mealHandler = mealHandler;
        this.historyHandler = historyHandler;
        this.targetHandler = targetHandler;
        this.progressHandler = progressHandler;
        this.reminderHandler = reminderHandler;
        this.subscriptionHandler = subscriptionHandler;
        this.settingsHandler = settingsHandler;
        this.mainMenuKeyboard = mainMenuKeyboard;
    }

    public void route(Update update, AbsSender bot) {
        try {
            if (update.hasPreCheckoutQuery()) {
                handlePreCheckout(update, bot);
                return;
            }

            if (!update.hasMessage() && !update.hasCallbackQuery()) return;

            Long telegramId;
            String firstName, lastName, username;

            if (update.hasMessage()) {
                Message msg = update.getMessage();
                telegramId = msg.getFrom().getId();
                firstName = msg.getFrom().getFirstName();
                lastName = msg.getFrom().getLastName();
                username = msg.getFrom().getUserName();
            } else {
                CallbackQuery cb = update.getCallbackQuery();
                telegramId = cb.getFrom().getId();
                firstName = cb.getFrom().getFirstName();
                lastName = cb.getFrom().getLastName();
                username = cb.getFrom().getUserName();
            }

            User user = userService.getOrCreateUser(telegramId, username, firstName, lastName);

            if (update.hasCallbackQuery()) {
                handleCallback(update.getCallbackQuery(), user, bot);
                return;
            }

            Message message = update.getMessage();

            if (message.hasSuccessfulPayment()) {
                execute(bot, subscriptionHandler.handleSuccessfulPayment(update, user));
                return;
            }

            if (message.hasContact()) {
                execute(bot, onboardingHandler.handleContact(update, user));
                return;
            }

            if (message.hasText()) {
                handleText(update, user, bot);
                return;
            }

            if (message.hasPhoto() && user.getState() == UserState.AWAITING_MEAL_PHOTO) {
                execute(bot, mealHandler.handlePhotoMeal(update, user, bot));
                return;
            }

            log.debug("Unhandled update type for telegramId={}", telegramId);
        } catch (Exception e) {
            log.error("Error routing update: {}", e.getMessage(), e);
        }
    }

    private void handleText(Update update, User user, AbsSender bot) throws Exception {
        String text = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();
        UserState state = user.getState();
        Language lang = user.getLanguage();

        if ("/start".equals(text)) {
            execute(bot, startHandler.handle(update, user));
            return;
        }
        if ("/terms".equals(text)) {
            execute(bot, SendMessage.builder().chatId(chatId)
                    .text(messageService.getMessage("terms.text", lang)).build());
            return;
        }
        if ("/support".equals(text)) {
            execute(bot, SendMessage.builder().chatId(chatId)
                    .text(messageService.getMessage("support.text", lang)).build());
            return;
        }

        switch (state) {
            case AWAITING_LANGUAGE -> execute(bot, onboardingHandler.handleLanguageSelection(update, user));
            case AWAITING_AGE -> execute(bot, onboardingHandler.handleAge(update, user));
            case AWAITING_HEIGHT -> execute(bot, onboardingHandler.handleHeight(update, user));
            case AWAITING_WEIGHT -> execute(bot, onboardingHandler.handleWeight(update, user));
            case AWAITING_SEX -> execute(bot, onboardingHandler.handleSex(update, user));
            case AWAITING_ACTIVITY -> execute(bot, onboardingHandler.handleActivity(update, user));
            case AWAITING_GOAL -> execute(bot, onboardingHandler.handleGoal(update, user));
            case AWAITING_MEAL_TEXT -> execute(bot, mealHandler.handleManualMeal(update, user));
            case AWAITING_REMINDER_TIME -> execute(bot, reminderHandler.handleAddReminder(update, user));
            default -> handleMainMenuText(update, user, bot, text, chatId, lang);
        }
    }

    private void handleMainMenuText(Update update, User user, AbsSender bot,
                                     String text, String chatId, Language lang) throws Exception {
        String photoMenu = messageService.getMessage("menu.photo", lang);
        String manualMenu = messageService.getMessage("menu.manual", lang);
        String historyMenu = messageService.getMessage("menu.history", lang);
        String targetsMenu = messageService.getMessage("menu.targets", lang);
        String progressMenu = messageService.getMessage("menu.progress", lang);
        String remindersMenu = messageService.getMessage("menu.reminders", lang);
        String subscriptionMenu = messageService.getMessage("menu.subscription", lang);
        String settingsMenu = messageService.getMessage("menu.settings", lang);

        if (text.equals(photoMenu)) {
            execute(bot, mealHandler.promptPhotoMeal(chatId, user));
        } else if (text.equals(manualMenu)) {
            execute(bot, mealHandler.promptManualMeal(chatId, user));
        } else if (text.equals(historyMenu)) {
            execute(bot, historyHandler.handleHistory(update, user));
        } else if (text.equals(targetsMenu)) {
            execute(bot, targetHandler.handleTargets(update, user));
        } else if (text.equals(progressMenu)) {
            execute(bot, progressHandler.handleProgress(update, user));
        } else if (text.equals(remindersMenu)) {
            execute(bot, reminderHandler.handleReminders(update, user));
        } else if (text.equals(subscriptionMenu)) {
            execute(bot, subscriptionHandler.handleSubscription(update, user));
        } else if (text.equals(settingsMenu)) {
            execute(bot, settingsHandler.handleSettings(update, user));
        } else {
            execute(bot, SendMessage.builder()
                    .chatId(chatId)
                    .text(messageService.getMessage("error.invalid_input", lang))
                    .replyMarkup(mainMenuKeyboard.buildMainMenu(lang))
                    .build());
        }
    }

    private void handleCallback(CallbackQuery callback, User user, AbsSender bot) throws Exception {
        String data = callback.getData();
        if (data == null) return;

        if (data.startsWith("meal:confirm:")) {
            execute(bot, mealHandler.handleMealConfirm(callback, user));
        } else if (data.startsWith("meal:cancel:")) {
            execute(bot, mealHandler.handleMealCancel(callback, user));
        } else if (data.startsWith("history:date:")) {
            execute(bot, historyHandler.handleHistoryNavigation(callback, user));
        } else if ("subscription:pay".equals(data)) {
            SendInvoice invoice = subscriptionHandler.handleSubscribePay(callback, user);
            bot.execute(invoice);
        } else if (data.startsWith("reminder:add")) {
            execute(bot, reminderHandler.handleAddReminderPrompt(callback, user));
        } else if (data.startsWith("reminder:remove:")) {
            execute(bot, reminderHandler.handleRemoveReminder(callback, user));
        } else if (data.startsWith("settings:lang:")) {
            execute(bot, settingsHandler.handleLanguageChange(callback, user));
        } else {
            log.warn("Unknown callback data: {}", data);
        }
    }

    private void handlePreCheckout(Update update, AbsSender bot) throws Exception {
        Long telegramId = update.getPreCheckoutQuery().getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseGet(() -> User.builder().telegramId(telegramId)
                        .language(Language.RU).state(UserState.IDLE).build());
        AnswerPreCheckoutQuery answer = subscriptionHandler.handlePreCheckout(update, user);
        bot.execute(answer);
    }

    private void execute(AbsSender bot, BotApiMethod<?> method) throws Exception {
        if (method != null) {
            bot.execute(method);
        }
    }
}
