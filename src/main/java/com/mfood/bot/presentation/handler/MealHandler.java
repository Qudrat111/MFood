package com.mfood.bot.presentation.handler;

import com.mfood.bot.application.dto.FoodItemDto;
import com.mfood.bot.application.service.MealService;
import com.mfood.bot.application.service.MessageService;
import com.mfood.bot.domain.enums.MealType;
import com.mfood.bot.domain.enums.UserState;
import com.mfood.bot.application.service.UserService;
import com.mfood.bot.domain.model.Meal;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.infrastructure.config.EdamamProperties;
import com.mfood.bot.infrastructure.config.TelegramBotProperties;
import com.mfood.bot.infrastructure.edamam.EdamamClient;
import com.mfood.bot.presentation.keyboard.InlineKeyboardFactory;
import com.mfood.bot.presentation.keyboard.MainMenuKeyboard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MealHandler {

    private final MealService mealService;
    private final MessageService messageService;
    private final EdamamClient edamamClient;
    private final EdamamProperties edamamProperties;
    private final TelegramBotProperties botProperties;
    private final InlineKeyboardFactory inlineKeyboardFactory;
    private final MainMenuKeyboard mainMenuKeyboard;
    private final UserService userService;

    public SendMessage handlePhotoMeal(Update update, User user, AbsSender bot) {
        String chatId = update.getMessage().getChatId().toString();
        userService.updateState(user.getTelegramId(), UserState.MAIN_MENU);

        List<PhotoSize> photos = update.getMessage().getPhoto();
        if (photos == null || photos.isEmpty()) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(messageService.getMessage("error.general", user.getLanguage()))
                    .build();
        }

        // Get the highest resolution photo
        PhotoSize photo = photos.stream()
                .max(Comparator.comparingInt(PhotoSize::getFileSize))
                .orElse(photos.get(photos.size() - 1));
        String fileId = photo.getFileId();

        try {
            GetFile getFileMethod = new GetFile(fileId);
            File file = bot.execute(getFileMethod);
            String fileUrl = "https://api.telegram.org/file/bot" +
                    botProperties.getToken() + "/" + file.getFilePath();

            List<FoodItemDto> items = edamamClient.analyzeImage(fileUrl,
                    edamamProperties.getAppId(), edamamProperties.getAppKey()).block();

            if (items == null || items.isEmpty()) {
                return SendMessage.builder()
                        .chatId(chatId)
                        .text(messageService.getMessage("meal.not_found", user.getLanguage()))
                        .build();
            }

            Meal meal = mealService.logMealFromEdamamItems(
                    user.getTelegramId(), items, fileId, MealType.SNACK);

            String saved = messageService.getMessage("meal.saved", user.getLanguage(),
                    formatNum(meal.getTotalCalories()),
                    formatNum(meal.getTotalProtein()),
                    formatNum(meal.getTotalFat()),
                    formatNum(meal.getTotalCarbs()));

            return SendMessage.builder()
                    .chatId(chatId)
                    .text(saved)
                    .replyMarkup(inlineKeyboardFactory.buildMealConfirmKeyboard(user.getLanguage(), meal.getId()))
                    .build();
        } catch (Exception e) {
            log.error("Error processing photo meal for telegramId={}: {}", user.getTelegramId(), e.getMessage());
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(messageService.getMessage("error.general", user.getLanguage()))
                    .build();
        }
    }

    public SendMessage handleManualMeal(Update update, User user) {
        String chatId = update.getMessage().getChatId().toString();
        String query = update.getMessage().getText();
        userService.updateState(user.getTelegramId(), UserState.MAIN_MENU);

        List<FoodItemDto> items = edamamClient.searchFood(
                query, edamamProperties.getAppId(), edamamProperties.getAppKey()).block();

        if (items == null || items.isEmpty()) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(messageService.getMessage("meal.not_found", user.getLanguage()))
                    .build();
        }

        Meal meal = mealService.logMealFromEdamamItems(
                user.getTelegramId(), items, null, MealType.SNACK);

        String saved = messageService.getMessage("meal.saved", user.getLanguage(),
                formatNum(meal.getTotalCalories()),
                formatNum(meal.getTotalProtein()),
                formatNum(meal.getTotalFat()),
                formatNum(meal.getTotalCarbs()));

        return SendMessage.builder()
                .chatId(chatId)
                .text(saved)
                .replyMarkup(inlineKeyboardFactory.buildMealConfirmKeyboard(user.getLanguage(), meal.getId()))
                .build();
    }

    public SendMessage promptPhotoMeal(String chatId, User user) {
        userService.updateState(user.getTelegramId(), UserState.AWAITING_MEAL_PHOTO);
        return SendMessage.builder()
                .chatId(chatId)
                .text(messageService.getMessage("meal.photo.prompt", user.getLanguage()))
                .build();
    }

    public SendMessage promptManualMeal(String chatId, User user) {
        userService.updateState(user.getTelegramId(), UserState.AWAITING_MEAL_TEXT);
        return SendMessage.builder()
                .chatId(chatId)
                .text(messageService.getMessage("meal.manual.prompt", user.getLanguage()))
                .build();
    }

    public SendMessage handleMealConfirm(CallbackQuery callback, User user) {
        // meal is already saved; this is a confirmation acknowledgement
        return SendMessage.builder()
                .chatId(callback.getMessage().getChatId().toString())
                .text(messageService.getMessage("meal.confirm", user.getLanguage()) + " ✅")
                .replyMarkup(mainMenuKeyboard.buildMainMenu(user.getLanguage()))
                .build();
    }

    public SendMessage handleMealCancel(CallbackQuery callback, User user) {
        String[] parts = callback.getData().split(":");
        if (parts.length >= 3) {
            try {
                Long mealId = Long.parseLong(parts[2]);
                mealService.deleteMeal(mealId, user.getTelegramId());
            } catch (NumberFormatException e) {
                log.warn("Invalid meal id in callback: {}", callback.getData());
            }
        }
        return SendMessage.builder()
                .chatId(callback.getMessage().getChatId().toString())
                .text(messageService.getMessage("meal.confirm.cancel", user.getLanguage()) + " ✅")
                .replyMarkup(mainMenuKeyboard.buildMainMenu(user.getLanguage()))
                .build();
    }

    private String formatNum(Double value) {
        if (value == null) return "0";
        return String.format("%.1f", value);
    }
}
