package com.mfood.bot.domain.model;

import com.mfood.bot.domain.enums.ActivityLevel;
import com.mfood.bot.domain.enums.Goal;
import com.mfood.bot.domain.enums.Sex;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex", length = 10)
    private Sex sex;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", length = 20)
    private ActivityLevel activityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal", length = 20)
    private Goal goal;

    @Column(name = "daily_calorie_target")
    private Double dailyCalorieTarget;

    @Column(name = "daily_protein_target")
    private Double dailyProteinTarget;

    @Column(name = "daily_fat_target")
    private Double dailyFatTarget;

    @Column(name = "daily_carbs_target")
    private Double dailyCarbsTarget;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
