package com.mfood.bot.presentation.keyboard;

import com.mfood.bot.application.service.MessageService;
import com.mfood.bot.domain.enums.Language;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InlineKeyboardFactory {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private final MessageService messageService;

    public InlineKeyboardMarkup buildMealConfirmKeyboard(Language lang, Long mealId) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        button(messageService.getMessage("meal.confirm.yes", lang), "meal:confirm:" + mealId),
                        button(messageService.getMessage("meal.confirm.edit", lang), "meal:edit:" + mealId),
                        button(messageService.getMessage("meal.confirm.cancel", lang), "meal:cancel:" + mealId)
                ))
                .build();
    }

    public InlineKeyboardMarkup buildSubscriptionKeyboard(Language lang) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        button(messageService.getMessage("subscription.subscribe", lang), "subscription:pay")
                ))
                .build();
    }

    public InlineKeyboardMarkup buildHistoryNavigationKeyboard(Language lang, LocalDate date) {
        String prevDate = date.minusDays(1).format(DATE_FMT);
        String nextDate = date.plusDays(1).format(DATE_FMT);
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        button("◀ " + prevDate, "history:date:" + prevDate),
                        button(date.format(DATE_FMT), "history:date:" + date.format(DATE_FMT)),
                        button(nextDate + " ▶", "history:date:" + nextDate)
                ))
                .build();
    }

    public InlineKeyboardMarkup buildLanguageChangeKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        button("🇺🇿 O'zbekcha", "settings:lang:UZ"),
                        button("🇷🇺 Русский", "settings:lang:RU")
                ))
                .build();
    }

    public InlineKeyboardMarkup buildReminderRemoveKeyboard(Language lang, Long reminderId) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        button("❌ Remove", "reminder:remove:" + reminderId)
                ))
                .build();
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
