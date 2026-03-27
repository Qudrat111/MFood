package com.mfood.bot.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionDto {
    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbs;
}
