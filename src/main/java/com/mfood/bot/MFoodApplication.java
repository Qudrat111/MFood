package com.mfood.bot;

import com.mfood.bot.infrastructure.config.TelegramBotProperties;
import com.mfood.bot.presentation.bot.MFoodBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
public class MFoodApplication {

    public static void main(String[] args) {
        SpringApplication.run(MFoodApplication.class, args);
    }

    @Bean
    public CommandLineRunner registerBot(MFoodBot bot, TelegramBotProperties properties) {
        return args -> {
            String token = properties.getToken();
            // Telegram bot tokens always have format "<number>:<string>" - skip if not configured
            if (token == null || token.isBlank() || !token.contains(":")) {
                log.warn("Telegram bot token not configured or invalid, skipping bot registration");
                return;
            }
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(bot);
                log.info("Telegram bot registered successfully: @{}", properties.getUsername());
            } catch (TelegramApiException e) {
                throw new RuntimeException("Failed to register Telegram bot", e);
            }
        };
    }
}
