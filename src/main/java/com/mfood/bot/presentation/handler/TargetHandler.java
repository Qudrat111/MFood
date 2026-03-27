package com.mfood.bot.presentation.handler;

import com.mfood.bot.application.dto.DailyTargetDto;
import com.mfood.bot.application.service.MessageService;
import com.mfood.bot.application.service.TargetService;
import com.mfood.bot.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetHandler {

    private final TargetService targetService;
    private final MessageService messageService;

    public SendMessage handleTargets(Update update, User user) {
        String chatId = update.getMessage().getChatId().toString();
        DailyTargetDto dto = targetService.getDailyProgress(user.getTelegramId(), LocalDate.now());

        StringBuilder sb = new StringBuilder();
        sb.append(messageService.getMessage("targets.title", user.getLanguage())).append("\n\n");

        sb.append(messageService.getMessage("targets.calories", user.getLanguage(),
                fmt(dto.getConsumedCalories()), fmt(dto.getTargetCalories()),
                pct(dto.getConsumedCalories(), dto.getTargetCalories()))).append("\n");
        sb.append(progressBar(dto.getConsumedCalories(), dto.getTargetCalories())).append("\n\n");

        sb.append(messageService.getMessage("targets.protein", user.getLanguage(),
                fmt(dto.getConsumedProtein()), fmt(dto.getTargetProtein()),
                pct(dto.getConsumedProtein(), dto.getTargetProtein()))).append("\n");
        sb.append(progressBar(dto.getConsumedProtein(), dto.getTargetProtein())).append("\n\n");

        sb.append(messageService.getMessage("targets.fat", user.getLanguage(),
                fmt(dto.getConsumedFat()), fmt(dto.getTargetFat()),
                pct(dto.getConsumedFat(), dto.getTargetFat()))).append("\n");
        sb.append(progressBar(dto.getConsumedFat(), dto.getTargetFat())).append("\n\n");

        sb.append(messageService.getMessage("targets.carbs", user.getLanguage(),
                fmt(dto.getConsumedCarbs()), fmt(dto.getTargetCarbs()),
                pct(dto.getConsumedCarbs(), dto.getTargetCarbs()))).append("\n");
        sb.append(progressBar(dto.getConsumedCarbs(), dto.getTargetCarbs()));

        return SendMessage.builder()
                .chatId(chatId)
                .text(sb.toString())
                .build();
    }

    private String progressBar(double consumed, double target) {
        if (target <= 0) return "░░░░░░░░░░";
        int filled = (int) Math.min(10, Math.round((consumed / target) * 10));
        return "▓".repeat(filled) + "░".repeat(10 - filled);
    }

    private String fmt(Double value) {
        if (value == null) return "0";
        return String.format("%.0f", value);
    }

    private String pct(Double consumed, Double target) {
        if (target == null || target <= 0 || consumed == null) return "0";
        return String.format("%.0f", (consumed / target) * 100);
    }
}
