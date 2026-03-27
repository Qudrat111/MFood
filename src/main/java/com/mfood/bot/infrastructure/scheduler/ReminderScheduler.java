package com.mfood.bot.infrastructure.scheduler;

import com.mfood.bot.application.service.ReminderService;
import com.mfood.bot.domain.model.Reminder;
import com.mfood.bot.presentation.bot.MFoodBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final ReminderService reminderService;
    private final MFoodBot bot;

    @Scheduled(fixedRate = 60000)
    public void checkReminders() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        List<Reminder> dueReminders = reminderService.getDueReminders(now);
        if (dueReminders.isEmpty()) return;

        log.info("Sending {} reminders for time {}", dueReminders.size(), now);
        for (Reminder reminder : dueReminders) {
            try {
                String message = reminder.getMessage() != null
                        ? reminder.getMessage()
                        : "🍽️ Time to log your meal!";
                bot.execute(SendMessage.builder()
                        .chatId(reminder.getUser().getTelegramId().toString())
                        .text(message)
                        .build());
            } catch (Exception e) {
                log.error("Failed to send reminder id={}: {}", reminder.getId(), e.getMessage());
            }
        }
    }
}
