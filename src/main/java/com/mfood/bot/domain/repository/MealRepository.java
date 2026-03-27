package com.mfood.bot.domain.repository;

import com.mfood.bot.domain.model.Meal;
import com.mfood.bot.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByUserAndMealDate(User user, LocalDate mealDate);
    List<Meal> findByUserAndMealDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
