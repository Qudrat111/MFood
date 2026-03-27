package com.mfood.bot.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTargetDto {
    private Double targetCalories;
    private Double targetProtein;
    private Double targetFat;
    private Double targetCarbs;

    private Double consumedCalories;
    private Double consumedProtein;
    private Double consumedFat;
    private Double consumedCarbs;

    public Double getRemainingCalories() {
        if (targetCalories == null || consumedCalories == null) return 0.0;
        return targetCalories - consumedCalories;
    }
}
