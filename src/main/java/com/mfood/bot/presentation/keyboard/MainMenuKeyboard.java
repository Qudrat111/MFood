package com.mfood.bot.presentation.keyboard;

import com.mfood.bot.application.service.MessageService;
import com.mfood.bot.domain.enums.Language;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MainMenuKeyboard {

    private final MessageService messageService;

    public ReplyKeyboardMarkup buildMainMenu(Language lang) {
        List<KeyboardRow> rows = new ArrayList<>();

        rows.add(row(
                messageService.getMessage("menu.photo", lang),
                messageService.getMessage("menu.manual", lang)
        ));
        rows.add(row(
                messageService.getMessage("menu.history", lang),
                messageService.getMessage("menu.targets", lang)
        ));
        rows.add(row(
                messageService.getMessage("menu.progress", lang),
                messageService.getMessage("menu.reminders", lang)
        ));
        rows.add(row(
                messageService.getMessage("menu.subscription", lang),
                messageService.getMessage("menu.settings", lang)
        ));

        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
    }

    public ReplyKeyboardMarkup buildPhoneRequestKeyboard(Language lang) {
        KeyboardButton phoneButton = KeyboardButton.builder()
                .text(messageService.getMessage("phone.share", lang))
                .requestContact(true)
                .build();
        KeyboardRow row = new KeyboardRow();
        row.add(phoneButton);
        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }

    public ReplyKeyboardMarkup buildLanguageKeyboard() {
        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row("🇺🇿 O'zbekcha", "🇷🇺 Русский")))
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }

    public ReplyKeyboardMarkup buildSexKeyboard(Language lang) {
        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row(
                        messageService.getMessage("onboarding.sex.male", lang),
                        messageService.getMessage("onboarding.sex.female", lang)
                )))
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }

    public ReplyKeyboardMarkup buildActivityKeyboard(Language lang) {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row(messageService.getMessage("onboarding.activity.sedentary", lang)));
        rows.add(row(messageService.getMessage("onboarding.activity.light", lang)));
        rows.add(row(messageService.getMessage("onboarding.activity.moderate", lang)));
        rows.add(row(messageService.getMessage("onboarding.activity.active", lang)));
        rows.add(row(messageService.getMessage("onboarding.activity.very_active", lang)));
        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }

    public ReplyKeyboardMarkup buildGoalKeyboard(Language lang) {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row(messageService.getMessage("onboarding.goal.lose", lang)));
        rows.add(row(messageService.getMessage("onboarding.goal.maintain", lang)));
        rows.add(row(messageService.getMessage("onboarding.goal.gain", lang)));
        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }

    public ReplyKeyboardMarkup buildMealTypeKeyboard(Language lang) {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row(
                messageService.getMessage("meal.type.breakfast", lang),
                messageService.getMessage("meal.type.lunch", lang)
        ));
        rows.add(row(
                messageService.getMessage("meal.type.dinner", lang),
                messageService.getMessage("meal.type.snack", lang)
        ));
        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }

    private KeyboardRow row(String... buttons) {
        KeyboardRow keyboardRow = new KeyboardRow();
        for (String b : buttons) {
            keyboardRow.add(new KeyboardButton(b));
        }
        return keyboardRow;
    }
}
