package com.mfood.bot.application.service;

import com.mfood.bot.application.dto.FoodItemDto;
import com.mfood.bot.application.dto.NutritionDto;
import com.mfood.bot.domain.enums.MealSource;
import com.mfood.bot.domain.enums.MealType;
import com.mfood.bot.domain.model.Meal;
import com.mfood.bot.domain.model.MealItem;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.domain.repository.MealRepository;
import com.mfood.bot.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;
    private final UserRepository userRepository;

    @Transactional
    public Meal logMealFromEdamamItems(Long telegramId, List<FoodItemDto> items,
                                       String photoFileId, MealType mealType) {
        User user = getUser(telegramId);

        Meal meal = Meal.builder()
                .user(user)
                .mealDate(LocalDate.now())
                .mealTime(LocalTime.now())
                .mealType(mealType)
                .photoFileId(photoFileId)
                .source(photoFileId != null ? MealSource.PHOTO : MealSource.MANUAL)
                .items(new ArrayList<>())
                .build();

        double totalCalories = 0, totalProtein = 0, totalFat = 0, totalCarbs = 0;
        for (FoodItemDto item : items) {
            MealItem mealItem = MealItem.builder()
                    .meal(meal)
                    .foodName(item.getLabel())
                    .quantity(item.getServingSize() != null ? item.getServingSize() : 100.0)
                    .unit(item.getUnit() != null ? item.getUnit() : "g")
                    .calories(orZero(item.getCalories()))
                    .protein(orZero(item.getProtein()))
                    .fat(orZero(item.getFat()))
                    .carbs(orZero(item.getCarbs()))
                    .edamamFoodId(item.getFoodId())
                    .build();
            meal.getItems().add(mealItem);
            totalCalories += mealItem.getCalories();
            totalProtein += mealItem.getProtein();
            totalFat += mealItem.getFat();
            totalCarbs += mealItem.getCarbs();
        }

        meal.setTotalCalories(round(totalCalories));
        meal.setTotalProtein(round(totalProtein));
        meal.setTotalFat(round(totalFat));
        meal.setTotalCarbs(round(totalCarbs));

        Meal saved = mealRepository.save(meal);
        log.info("Saved meal id={} for user telegramId={}, calories={}", saved.getId(), telegramId, saved.getTotalCalories());
        return saved;
    }

    @Transactional
    public Meal logManualMeal(Long telegramId, String foodText, Double quantity,
                               NutritionDto nutrition, MealType mealType) {
        User user = getUser(telegramId);

        MealItem item = MealItem.builder()
                .foodName(foodText)
                .quantity(quantity != null ? quantity : 100.0)
                .unit("g")
                .calories(orZero(nutrition.getCalories()))
                .protein(orZero(nutrition.getProtein()))
                .fat(orZero(nutrition.getFat()))
                .carbs(orZero(nutrition.getCarbs()))
                .build();

        Meal meal = Meal.builder()
                .user(user)
                .mealDate(LocalDate.now())
                .mealTime(LocalTime.now())
                .mealType(mealType)
                .source(MealSource.MANUAL)
                .totalCalories(round(orZero(nutrition.getCalories())))
                .totalProtein(round(orZero(nutrition.getProtein())))
                .totalFat(round(orZero(nutrition.getFat())))
                .totalCarbs(round(orZero(nutrition.getCarbs())))
                .items(new ArrayList<>())
                .build();
        item.setMeal(meal);
        meal.getItems().add(item);

        return mealRepository.save(meal);
    }

    @Transactional(readOnly = true)
    public List<Meal> getMealsForDate(Long telegramId, LocalDate date) {
        User user = getUser(telegramId);
        return mealRepository.findByUserAndMealDate(user, date);
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, List<Meal>> getMealHistory(Long telegramId, int days) {
        User user = getUser(telegramId);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        List<Meal> meals = mealRepository.findByUserAndMealDateBetween(user, startDate, endDate);
        return meals.stream().collect(Collectors.groupingBy(Meal::getMealDate, TreeMap::new, Collectors.toList()));
    }

    @Transactional
    public boolean deleteMeal(Long mealId, Long telegramId) {
        User user = getUser(telegramId);
        return mealRepository.findById(mealId)
                .filter(meal -> meal.getUser().getId().equals(user.getId()))
                .map(meal -> {
                    mealRepository.delete(meal);
                    return true;
                })
                .orElse(false);
    }

    private User getUser(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException("User not found for telegramId=" + telegramId));
    }

    private double orZero(Double value) {
        return value != null ? value : 0.0;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
