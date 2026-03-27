package com.mfood.bot.domain.repository;

import com.mfood.bot.domain.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByUserIdAndActive(Long userId, boolean active);
    List<Reminder> findByActiveAndReminderTime(boolean active, LocalTime reminderTime);
}
