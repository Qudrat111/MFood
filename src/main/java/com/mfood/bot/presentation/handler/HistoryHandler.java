package com.mfood.bot.presentation.handler;

import com.mfood.bot.application.service.MealService;
import com.mfood.bot.application.service.MessageService;
import com.mfood.bot.domain.model.Meal;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.presentation.keyboard.InlineKeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HistoryHandler {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final MealService mealService;
    private final MessageService messageService;
    private final InlineKeyboardFactory inlineKeyboardFactory;

    public SendMessage handleHistory(Update update, User user) {
        return showHistory(update.getMessage().getChatId().toString(), user, LocalDate.now());
    }

    public SendMessage handleHistoryNavigation(CallbackQuery callback, User user) {
        String[] parts = callback.getData().split(":");
        LocalDate date = parts.length >= 3 ? LocalDate.parse(parts[2]) : LocalDate.now();
        return showHistory(callback.getMessage().getChatId().toString(), user, date);
    }

    private SendMessage showHistory(String chatId, User user, LocalDate date) {
        List<Meal> meals = mealService.getMealsForDate(user.getTelegramId(), date);
        String title = messageService.getMessage("history.title", user.getLanguage(),
                date.format(DATE_FMT));

        StringBuilder sb = new StringBuilder(title).append("\n\n");
        if (meals.isEmpty()) {
            sb.append(messageService.getMessage("history.empty", user.getLanguage()));
        } else {
            double totalCal = 0, totalProt = 0, totalFat = 0, totalCarbs = 0;
            for (Meal meal : meals) {
                sb.append("🍽️ ").append(meal.getMealType()).append(": ")
                        .append(String.format("%.0f", meal.getTotalCalories())).append(" kcal\n");
                totalCal += meal.getTotalCalories() != null ? meal.getTotalCalories() : 0;
                totalProt += meal.getTotalProtein() != null ? meal.getTotalProtein() : 0;
                totalFat += meal.getTotalFat() != null ? meal.getTotalFat() : 0;
                totalCarbs += meal.getTotalCarbs() != null ? meal.getTotalCarbs() : 0;
            }
            sb.append("\n").append(messageService.getMessage("history.total", user.getLanguage(),
                    String.format("%.0f", totalCal),
                    String.format("%.1f", totalProt),
                    String.format("%.1f", totalFat),
                    String.format("%.1f", totalCarbs)));
        }

        return SendMessage.builder()
                .chatId(chatId)
                .text(sb.toString())
                .replyMarkup(inlineKeyboardFactory.buildHistoryNavigationKeyboard(user.getLanguage(), date))
                .build();
    }
}
