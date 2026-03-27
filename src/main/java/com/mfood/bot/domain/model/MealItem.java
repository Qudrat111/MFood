package com.mfood.bot.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meal_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @Column(name = "food_name", length = 500, nullable = false)
    private String foodName;

    @Column(name = "quantity")
    @Builder.Default
    private Double quantity = 100.0;

    @Column(name = "unit", length = 50)
    @Builder.Default
    private String unit = "g";

    @Column(name = "calories")
    @Builder.Default
    private Double calories = 0.0;

    @Column(name = "protein")
    @Builder.Default
    private Double protein = 0.0;

    @Column(name = "fat")
    @Builder.Default
    private Double fat = 0.0;

    @Column(name = "carbs")
    @Builder.Default
    private Double carbs = 0.0;

    @Column(name = "edamam_food_id")
    private String edamamFoodId;
}
