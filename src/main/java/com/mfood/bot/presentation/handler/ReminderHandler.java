package com.mfood.bot.presentation.handler;

import com.mfood.bot.application.service.MessageService;
import com.mfood.bot.application.service.ReminderService;
import com.mfood.bot.application.service.UserService;
import com.mfood.bot.domain.enums.UserState;
import com.mfood.bot.domain.model.Reminder;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.presentation.keyboard.InlineKeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderHandler {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private final ReminderService reminderService;
    private final MessageService messageService;
    private final UserService userService;
    private final InlineKeyboardFactory inlineKeyboardFactory;

    public SendMessage handleReminders(Update update, User user) {
        String chatId = update.getMessage().getChatId().toString();
        List<Reminder> reminders = reminderService.getUserReminders(user.getTelegramId());

        StringBuilder sb = new StringBuilder();
        sb.append(messageService.getMessage("reminders.title", user.getLanguage())).append("\n\n");

        if (reminders.isEmpty()) {
            sb.append(messageService.getMessage("reminders.none", user.getLanguage()));
        } else {
            for (Reminder r : reminders) {
                sb.append("🔔 ").append(r.getReminderTime().format(TIME_FMT)).append("\n");
            }
        }

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(InlineKeyboardButton.builder()
                .text(messageService.getMessage("reminders.add", user.getLanguage()))
                .callbackData("reminder:add")
                .build()));
        for (Reminder r : reminders) {
            keyboard.add(List.of(InlineKeyboardButton.builder()
                    .text("❌ " + r.getReminderTime().format(TIME_FMT))
                    .callbackData("reminder:remove:" + r.getId())
                    .build()));
        }

        return SendMessage.builder()
                .chatId(chatId)
                .text(sb.toString())
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(keyboard).build())
                .build();
    }

    public SendMessage handleAddReminderPrompt(CallbackQuery callback, User user) {
        userService.updateState(user.getTelegramId(), UserState.AWAITING_REMINDER_TIME);
        return SendMessage.builder()
                .chatId(callback.getMessage().getChatId().toString())
                .text(messageService.getMessage("reminders.time_prompt", user.getLanguage()))
                .build();
    }

    public SendMessage handleAddReminder(Update update, User user) {
        String text = update.getMessage().getText().trim();
        try {
            LocalTime time = LocalTime.parse(text, TIME_FMT);
            Reminder reminder = reminderService.addReminder(user.getTelegramId(), time);
            userService.updateState(user.getTelegramId(), UserState.MAIN_MENU);
            return SendMessage.builder()
                    .chatId(update.getMessage().getChatId().toString())
                    .text(messageService.getMessage("reminders.added", user.getLanguage(),
                            reminder.getReminderTime().format(TIME_FMT)))
                    .build();
        } catch (DateTimeParseException e) {
            return SendMessage.builder()
                    .chatId(update.getMessage().getChatId().toString())
                    .text(messageService.getMessage("error.invalid_input", user.getLanguage()))
                    .build();
        }
    }

    public SendMessage handleRemoveReminder(CallbackQuery callback, User user) {
        String[] parts = callback.getData().split(":");
        if (parts.length >= 3) {
            try {
                Long reminderId = Long.parseLong(parts[2]);
                reminderService.removeReminder(reminderId, user.getTelegramId());
            } catch (NumberFormatException e) {
                log.warn("Invalid reminder id in callback: {}", callback.getData());
            }
        }
        return SendMessage.builder()
                .chatId(callback.getMessage().getChatId().toString())
                .text("✅ Reminder removed.")
                .build();
    }
}
