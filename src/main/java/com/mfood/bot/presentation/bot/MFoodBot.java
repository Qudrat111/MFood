package com.mfood.bot.presentation.bot;

import com.mfood.bot.infrastructure.config.TelegramBotProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class MFoodBot extends TelegramLongPollingBot {

    private final TelegramBotProperties properties;
    private final TelegramUpdateRouter router;

    public MFoodBot(TelegramBotProperties properties, TelegramUpdateRouter router) {
        super(properties.getToken());
        this.properties = properties;
        this.router = router;
    }

    @Override
    public String getBotUsername() {
        return properties.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            router.route(update, this);
        } catch (Exception e) {
            log.error("Unhandled error in onUpdateReceived: {}", e.getMessage(), e);
        }
    }
}
