package com.mfood.bot.presentation.handler;

import com.mfood.bot.application.service.MessageService;
import com.mfood.bot.application.service.UserService;
import com.mfood.bot.domain.enums.Language;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.presentation.keyboard.InlineKeyboardFactory;
import com.mfood.bot.presentation.keyboard.MainMenuKeyboard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettingsHandler {

    private final MessageService messageService;
    private final UserService userService;
    private final InlineKeyboardFactory inlineKeyboardFactory;
    private final MainMenuKeyboard mainMenuKeyboard;

    public SendMessage handleSettings(Update update, User user) {
        String chatId = update.getMessage().getChatId().toString();
        StringBuilder sb = new StringBuilder();
        sb.append(messageService.getMessage("settings.title", user.getLanguage())).append("\n\n");
        sb.append(messageService.getMessage("settings.language", user.getLanguage())).append("\n");
        sb.append(messageService.getMessage("settings.profile", user.getLanguage()));

        return SendMessage.builder()
                .chatId(chatId)
                .text(sb.toString())
                .replyMarkup(inlineKeyboardFactory.buildLanguageChangeKeyboard())
                .build();
    }

    public SendMessage handleLanguageChange(CallbackQuery callback, User user) {
        String[] parts = callback.getData().split(":");
        if (parts.length >= 3) {
            Language newLang = "UZ".equals(parts[2]) ? Language.UZ : Language.RU;
            userService.updateLanguage(user.getTelegramId(), newLang);
            user = userService.findByTelegramId(user.getTelegramId()).orElse(user);
        }
        return SendMessage.builder()
                .chatId(callback.getMessage().getChatId().toString())
                .text(messageService.getMessage("settings.title", user.getLanguage()) + " ✅")
                .replyMarkup(mainMenuKeyboard.buildMainMenu(user.getLanguage()))
                .build();
    }
}
