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
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProgressHandler {

    private final TargetService targetService;
    private final MessageService messageService;

    public SendMessage handleProgress(Update update, User user) {
        String chatId = update.getMessage().getChatId().toString();
        List<DailyTargetDto> weekly = targetService.getWeeklyProgress(user.getTelegramId());

        StringBuilder sb = new StringBuilder();
        sb.append(messageService.getMessage("progress.title", user.getLanguage())).append("\n\n");

        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        for (int i = 0; i < weekly.size(); i++) {
            DailyTargetDto dto = weekly.get(i);
            LocalDate date = today.minusDays(weekly.size() - 1 - i);
            double consumed = dto.getConsumedCalories() != null ? dto.getConsumedCalories() : 0;
            double target = dto.getTargetCalories() != null ? dto.getTargetCalories() : 2000;

            String bar = progressBar(consumed, target);
            sb.append(date.format(fmt)).append(" ")
                    .append(bar).append(" ")
                    .append(String.format("%.0f", consumed)).append("/")
                    .append(String.format("%.0f", target)).append(" kcal\n");
        }

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
}
