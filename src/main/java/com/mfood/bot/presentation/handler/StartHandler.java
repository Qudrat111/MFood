package com.mfood.bot.presentation.handler;

import com.mfood.bot.application.service.MessageService;
import com.mfood.bot.application.service.UserService;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.presentation.keyboard.MainMenuKeyboard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartHandler {

    private final MessageService messageService;
    private final MainMenuKeyboard mainMenuKeyboard;
    private final UserService userService;

    public SendMessage handle(Update update, User user) {
        log.info("StartHandler for telegramId={}", user.getTelegramId());
        String chatId = update.getMessage().getChatId().toString();

        String welcome = messageService.getMessage("welcome", user.getLanguage());
        String langSelect = messageService.getMessage("language.select", user.getLanguage());

        return SendMessage.builder()
                .chatId(chatId)
                .text(welcome + "\n\n" + langSelect)
                .replyMarkup(mainMenuKeyboard.buildLanguageKeyboard())
                .build();
    }
}
