package com.mfood.bot.application.service;

import com.mfood.bot.application.dto.DailyTargetDto;
import com.mfood.bot.domain.enums.Sex;
import com.mfood.bot.domain.model.Meal;
import com.mfood.bot.domain.model.Profile;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.domain.repository.MealRepository;
import com.mfood.bot.domain.repository.ProfileRepository;
import com.mfood.bot.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TargetService {

    private final ProfileRepository profileRepository;
    private final MealRepository mealRepository;
    private final UserRepository userRepository;

    /**
     * Computes daily targets using Mifflin-St Jeor formula.
     */
    public DailyTargetDto computeDailyTargets(Profile profile) {
        if (profile.getAge() == null || profile.getHeightCm() == null
                || profile.getWeightKg() == null || profile.getSex() == null
                || profile.getActivityLevel() == null || profile.getGoal() == null) {
            return emptyTargets();
        }

        double weight = profile.getWeightKg();
        double height = profile.getHeightCm();
        int age = profile.getAge();

        double bmr;
        if (profile.getSex() == Sex.MALE) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        }

        double tdee = bmr * profile.getActivityLevel().getMultiplier();
        double totalCalories = tdee + profile.getGoal().getCalorieAdjustment();
        totalCalories = Math.max(totalCalories, 1200); // minimum safe calories

        double protein = weight * 2.0; // 2g per kg
        double fat = (totalCalories * 0.25) / 9.0; // 25% of calories from fat
        double carbsCalories = totalCalories - (protein * 4) - (fat * 9);
        double carbs = carbsCalories / 4.0;

        return DailyTargetDto.builder()
                .targetCalories(round(totalCalories))
                .targetProtein(round(protein))
                .targetFat(round(fat))
                .targetCarbs(round(Math.max(carbs, 0)))
                .consumedCalories(0.0)
                .consumedProtein(0.0)
                .consumedFat(0.0)
                .consumedCarbs(0.0)
                .build();
    }

    @Transactional(readOnly = true)
    public DailyTargetDto getDailyProgress(Long telegramId, LocalDate date) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);

        DailyTargetDto targets = profile != null ? computeDailyTargets(profile) : emptyTargets();

        List<Meal> meals = mealRepository.findByUserAndMealDate(user, date);
        double consumedCalories = meals.stream().mapToDouble(m -> m.getTotalCalories() != null ? m.getTotalCalories() : 0).sum();
        double consumedProtein = meals.stream().mapToDouble(m -> m.getTotalProtein() != null ? m.getTotalProtein() : 0).sum();
        double consumedFat = meals.stream().mapToDouble(m -> m.getTotalFat() != null ? m.getTotalFat() : 0).sum();
        double consumedCarbs = meals.stream().mapToDouble(m -> m.getTotalCarbs() != null ? m.getTotalCarbs() : 0).sum();

        targets.setConsumedCalories(round(consumedCalories));
        targets.setConsumedProtein(round(consumedProtein));
        targets.setConsumedFat(round(consumedFat));
        targets.setConsumedCarbs(round(consumedCarbs));
        return targets;
    }

    @Transactional(readOnly = true)
    public List<DailyTargetDto> getWeeklyProgress(Long telegramId) {
        LocalDate today = LocalDate.now();
        List<DailyTargetDto> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            result.add(getDailyProgress(telegramId, today.minusDays(i)));
        }
        return result;
    }

    private DailyTargetDto emptyTargets() {
        return DailyTargetDto.builder()
                .targetCalories(2000.0)
                .targetProtein(150.0)
                .targetFat(55.0)
                .targetCarbs(250.0)
                .consumedCalories(0.0)
                .consumedProtein(0.0)
                .consumedFat(0.0)
                .consumedCarbs(0.0)
                .build();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
