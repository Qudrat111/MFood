package com.mfood.bot.application.service;

import com.mfood.bot.application.dto.DailyTargetDto;
import com.mfood.bot.domain.enums.ActivityLevel;
import com.mfood.bot.domain.enums.Goal;
import com.mfood.bot.domain.enums.Sex;
import com.mfood.bot.domain.model.Profile;
import com.mfood.bot.domain.repository.MealRepository;
import com.mfood.bot.domain.repository.ProfileRepository;
import com.mfood.bot.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@ExtendWith(MockitoExtension.class)
class TargetServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private MealRepository mealRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TargetService targetService;

    private Profile maleProfile;
    private Profile femaleProfile;

    @BeforeEach
    void setUp() {
        maleProfile = Profile.builder()
                .age(25)
                .sex(Sex.MALE)
                .heightCm(175.0)
                .weightKg(70.0)
                .activityLevel(ActivityLevel.MODERATE)
                .goal(Goal.MAINTAIN)
                .build();

        femaleProfile = Profile.builder()
                .age(30)
                .sex(Sex.FEMALE)
                .heightCm(165.0)
                .weightKg(60.0)
                .activityLevel(ActivityLevel.LIGHT)
                .goal(Goal.LOSE_WEIGHT)
                .build();
    }

    @Test
    void computeDailyTargets_maleModerateMaintain_correctBMR() {
        // BMR for male: 10*70 + 6.25*175 - 5*25 + 5 = 700 + 1093.75 - 125 + 5 = 1673.75
        // TDEE = 1673.75 * 1.55 = 2594.3125
        // Goal: MAINTAIN (0), so total = 2594.3
        DailyTargetDto dto = targetService.computeDailyTargets(maleProfile);

        assertThat(dto.getTargetCalories()).isCloseTo(2594.3, within(2.0));
        // Protein: 70 * 2 = 140g
        assertThat(dto.getTargetProtein()).isCloseTo(140.0, within(1.0));
        // Fat: (2594.3 * 0.25) / 9 = ~72g
        assertThat(dto.getTargetFat()).isCloseTo(72.0, within(2.0));
    }

    @Test
    void computeDailyTargets_femaleLightLoseWeight_reducedCalories() {
        // BMR for female: 10*60 + 6.25*165 - 5*30 - 161 = 600 + 1031.25 - 150 - 161 = 1320.25
        // TDEE = 1320.25 * 1.375 = 1815.34
        // Goal: LOSE_WEIGHT (-500), so total = 1315.34 -> floored to min 1200
        DailyTargetDto dto = targetService.computeDailyTargets(femaleProfile);

        assertThat(dto.getTargetCalories()).isGreaterThanOrEqualTo(1200.0);
        assertThat(dto.getTargetCalories()).isLessThan(2500.0);
        // Protein: 60 * 2 = 120g
        assertThat(dto.getTargetProtein()).isCloseTo(120.0, within(1.0));
    }

    @Test
    void computeDailyTargets_gainWeightGoal_addedCalories() {
        maleProfile.setGoal(Goal.GAIN_WEIGHT);
        DailyTargetDto maintain = targetService.computeDailyTargets(
                Profile.builder()
                        .age(25).sex(Sex.MALE).heightCm(175.0).weightKg(70.0)
                        .activityLevel(ActivityLevel.MODERATE).goal(Goal.MAINTAIN)
                        .build());
        DailyTargetDto gain = targetService.computeDailyTargets(maleProfile);

        assertThat(gain.getTargetCalories()).isCloseTo(maintain.getTargetCalories() + 500, within(2.0));
    }

    @Test
    void computeDailyTargets_incompleteProfile_returnsDefaults() {
        Profile incomplete = Profile.builder().build();
        DailyTargetDto dto = targetService.computeDailyTargets(incomplete);

        // Should return default values, not throw
        assertThat(dto).isNotNull();
        assertThat(dto.getTargetCalories()).isEqualTo(2000.0);
    }

    @Test
    void computeDailyTargets_sedentaryVsVeryActive_differentCalories() {
        maleProfile.setActivityLevel(ActivityLevel.SEDENTARY);
        DailyTargetDto sedentary = targetService.computeDailyTargets(maleProfile);

        maleProfile.setActivityLevel(ActivityLevel.VERY_ACTIVE);
        DailyTargetDto veryActive = targetService.computeDailyTargets(maleProfile);

        assertThat(veryActive.getTargetCalories()).isGreaterThan(sedentary.getTargetCalories());
    }
}
