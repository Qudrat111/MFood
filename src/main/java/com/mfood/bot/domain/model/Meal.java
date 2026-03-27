package com.mfood.bot.domain.model;

import com.mfood.bot.domain.enums.MealSource;
import com.mfood.bot.domain.enums.MealType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @Column(name = "meal_time")
    private LocalTime mealTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", length = 20)
    @Builder.Default
    private MealType mealType = MealType.SNACK;

    @Column(name = "total_calories")
    @Builder.Default
    private Double totalCalories = 0.0;

    @Column(name = "total_protein")
    @Builder.Default
    private Double totalProtein = 0.0;

    @Column(name = "total_fat")
    @Builder.Default
    private Double totalFat = 0.0;

    @Column(name = "total_carbs")
    @Builder.Default
    private Double totalCarbs = 0.0;

    @Column(name = "photo_file_id", length = 500)
    private String photoFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20)
    @Builder.Default
    private MealSource source = MealSource.MANUAL;

    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MealItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
