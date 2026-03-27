package com.mfood.bot.application.service;

import com.mfood.bot.domain.model.Reminder;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.domain.repository.ReminderRepository;
import com.mfood.bot.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    @Transactional
    public Reminder addReminder(Long telegramId, LocalTime reminderTime) {
        User user = getUser(telegramId);
        Reminder reminder = Reminder.builder()
                .user(user)
                .reminderTime(reminderTime)
                .message("Time to log your meal! 🍽️")
                .active(true)
                .build();
        Reminder saved = reminderRepository.save(reminder);
        log.info("Reminder added for telegramId={} at {}", telegramId, reminderTime);
        return saved;
    }

    @Transactional
    public boolean removeReminder(Long reminderId, Long telegramId) {
        User user = getUser(telegramId);
        return reminderRepository.findById(reminderId)
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .map(r -> {
                    r.setActive(false);
                    reminderRepository.save(r);
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<Reminder> getUserReminders(Long telegramId) {
        User user = getUser(telegramId);
        return reminderRepository.findByUserIdAndActive(user.getId(), true);
    }

    @Transactional(readOnly = true)
    public List<Reminder> getDueReminders(LocalTime currentTime) {
        // Match reminders within the same minute
        return reminderRepository.findByActiveAndReminderTime(true, currentTime.withSecond(0).withNano(0));
    }

    private User getUser(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException("User not found for telegramId=" + telegramId));
    }
}
