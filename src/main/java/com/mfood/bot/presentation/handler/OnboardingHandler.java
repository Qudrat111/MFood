package com.mfood.bot.presentation.handler;

import com.mfood.bot.application.service.MessageService;
import com.mfood.bot.application.service.OnboardingService;
import com.mfood.bot.application.service.UserService;
import com.mfood.bot.domain.enums.Language;
import com.mfood.bot.domain.enums.UserState;
import com.mfood.bot.domain.model.Profile;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.presentation.keyboard.MainMenuKeyboard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnboardingHandler {

    private final MessageService messageService;
    private final UserService userService;
    private final OnboardingService onboardingService;
    private final MainMenuKeyboard mainMenuKeyboard;

    public SendMessage handleLanguageSelection(Update update, User user) {
        String text = update.getMessage().getText();
        Language language = text.contains("O'zbekcha") || text.contains("🇺🇿") ? Language.UZ : Language.RU;
        User updated = userService.updateLanguage(user.getTelegramId(), language);
        userService.updateState(user.getTelegramId(), UserState.AWAITING_PHONE);

        return SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text(messageService.getMessage("phone.request", updated.getLanguage()))
                .replyMarkup(mainMenuKeyboard.buildPhoneRequestKeyboard(updated.getLanguage()))
                .build();
    }

    public SendMessage handleContact(Update update, User user) {
        Contact contact = update.getMessage().getContact();
        userService.updatePhone(user.getTelegramId(), contact.getPhoneNumber());
        userService.updateState(user.getTelegramId(), UserState.AWAITING_AGE);

        return SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text(messageService.getMessage("phone.received", user.getLanguage())
                        + "\n\n" + messageService.getMessage("onboarding.age", user.getLanguage()))
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();
    }

    public SendMessage handleAge(Update update, User user) {
        boolean ok = onboardingService.handleAge(user.getTelegramId(), update.getMessage().getText());
        if (!ok) {
            return errorMessage(update, user, "error.invalid_input");
        }
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text(messageService.getMessage("onboarding.height", user.getLanguage()))
                .build();
    }

    public SendMessage handleHeight(Update update, User user) {
        boolean ok = onboardingService.handleHeight(user.getTelegramId(), update.getMessage().getText());
        if (!ok) {
            return errorMessage(update, user, "error.invalid_input");
        }
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text(messageService.getMessage("onboarding.weight", user.getLanguage()))
                .build();
    }

    public SendMessage handleWeight(Update update, User user) {
        boolean ok = onboardingService.handleWeight(user.getTelegramId(), update.getMessage().getText());
        if (!ok) {
            return errorMessage(update, user, "error.invalid_input");
        }
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text(messageService.getMessage("onboarding.sex", user.getLanguage()))
                .replyMarkup(mainMenuKeyboard.buildSexKeyboard(user.getLanguage()))
                .build();
    }

    public SendMessage handleSex(Update update, User user) {
        boolean ok = onboardingService.handleSex(user.getTelegramId(), update.getMessage().getText());
        if (!ok) {
            return errorMessage(update, user, "error.invalid_input");
        }
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text(messageService.getMessage("onboarding.activity", user.getLanguage()))
                .replyMarkup(mainMenuKeyboard.buildActivityKeyboard(user.getLanguage()))
                .build();
    }

    public SendMessage handleActivity(Update update, User user) {
        boolean ok = onboardingService.handleActivity(user.getTelegramId(), update.getMessage().getText());
        if (!ok) {
            return errorMessage(update, user, "error.invalid_input");
        }
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text(messageService.getMessage("onboarding.goal", user.getLanguage()))
                .replyMarkup(mainMenuKeyboard.buildGoalKeyboard(user.getLanguage()))
                .build();
    }

    public SendMessage handleGoal(Update update, User user) {
        boolean ok = onboardingService.handleGoal(user.getTelegramId(), update.getMessage().getText());
        if (!ok) {
            return errorMessage(update, user, "error.invalid_input");
        }
        Profile profile = onboardingService.completeOnboarding(user.getTelegramId());
        String completionMsg = messageService.getMessage("onboarding.complete", user.getLanguage(),
                formatNum(profile.getDailyCalorieTarget()),
                formatNum(profile.getDailyProteinTarget()),
                formatNum(profile.getDailyFatTarget()),
                formatNum(profile.getDailyCarbsTarget()));

        return SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text(completionMsg)
                .replyMarkup(mainMenuKeyboard.buildMainMenu(user.getLanguage()))
                .build();
    }

    private SendMessage errorMessage(Update update, User user, String key) {
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text(messageService.getMessage(key, user.getLanguage()))
                .build();
    }

    private String formatNum(Double value) {
        if (value == null) return "0";
        return String.valueOf(Math.round(value));
    }
}
