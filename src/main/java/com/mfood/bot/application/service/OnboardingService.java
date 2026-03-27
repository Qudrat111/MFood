package com.mfood.bot.application.service;

import com.mfood.bot.domain.enums.ActivityLevel;
import com.mfood.bot.domain.enums.Goal;
import com.mfood.bot.domain.enums.Sex;
import com.mfood.bot.domain.enums.UserState;
import com.mfood.bot.domain.model.Profile;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.domain.repository.ProfileRepository;
import com.mfood.bot.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final TargetService targetService;

    @Transactional
    public void startOnboarding(Long telegramId) {
        userService.updateState(telegramId, UserState.AWAITING_LANGUAGE);
    }

    @Transactional
    public boolean handleAge(Long telegramId, String ageText) {
        try {
            int age = Integer.parseInt(ageText.trim());
            if (age < 10 || age > 120) return false;
            Profile profile = getOrCreateProfile(telegramId);
            profile.setAge(age);
            profileRepository.save(profile);
            userService.updateState(telegramId, UserState.AWAITING_HEIGHT);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Transactional
    public boolean handleHeight(Long telegramId, String heightText) {
        try {
            double height = Double.parseDouble(heightText.trim());
            if (height < 50 || height > 300) return false;
            Profile profile = getOrCreateProfile(telegramId);
            profile.setHeightCm(height);
            profileRepository.save(profile);
            userService.updateState(telegramId, UserState.AWAITING_WEIGHT);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Transactional
    public boolean handleWeight(Long telegramId, String weightText) {
        try {
            double weight = Double.parseDouble(weightText.trim());
            if (weight < 10 || weight > 500) return false;
            Profile profile = getOrCreateProfile(telegramId);
            profile.setWeightKg(weight);
            profileRepository.save(profile);
            userService.updateState(telegramId, UserState.AWAITING_SEX);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Transactional
    public boolean handleSex(Long telegramId, String sexText) {
        Sex sex = parseSex(sexText);
        if (sex == null) return false;
        Profile profile = getOrCreateProfile(telegramId);
        profile.setSex(sex);
        profileRepository.save(profile);
        userService.updateState(telegramId, UserState.AWAITING_ACTIVITY);
        return true;
    }

    @Transactional
    public boolean handleActivity(Long telegramId, String activityText) {
        ActivityLevel level = parseActivityLevel(activityText);
        if (level == null) return false;
        Profile profile = getOrCreateProfile(telegramId);
        profile.setActivityLevel(level);
        profileRepository.save(profile);
        userService.updateState(telegramId, UserState.AWAITING_GOAL);
        return true;
    }

    @Transactional
    public boolean handleGoal(Long telegramId, String goalText) {
        Goal goal = parseGoal(goalText);
        if (goal == null) return false;
        Profile profile = getOrCreateProfile(telegramId);
        profile.setGoal(goal);
        profileRepository.save(profile);
        return true;
    }

    @Transactional
    public Profile completeOnboarding(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Profile profile = getOrCreateProfile(telegramId);

        // Compute and save targets
        var targets = targetService.computeDailyTargets(profile);
        profile.setDailyCalorieTarget(targets.getTargetCalories());
        profile.setDailyProteinTarget(targets.getTargetProtein());
        profile.setDailyFatTarget(targets.getTargetFat());
        profile.setDailyCarbsTarget(targets.getTargetCarbs());

        Profile saved = profileRepository.save(profile);
        userService.updateState(telegramId, UserState.MAIN_MENU);
        log.info("Onboarding complete for telegramId={}, calories={}", telegramId, targets.getTargetCalories());
        return saved;
    }

    private Profile getOrCreateProfile(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return profileRepository.findByUserId(user.getId())
                .orElseGet(() -> Profile.builder().user(user).build());
    }

    private Sex parseSex(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();
        if (lower.contains("erkak") || lower.contains("мужчина") || lower.contains("male") || lower.contains("👨")) {
            return Sex.MALE;
        }
        if (lower.contains("ayol") || lower.contains("женщина") || lower.contains("female") || lower.contains("👩")) {
            return Sex.FEMALE;
        }
        return null;
    }

    private ActivityLevel parseActivityLevel(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();
        if (lower.contains("kam") || lower.contains("сидячий") || lower.contains("sedentary") || lower.contains("🪑")) {
            return ActivityLevel.SEDENTARY;
        }
        if (lower.contains("engil") || lower.contains("лёгкая") || lower.contains("light") || lower.contains("🚶")) {
            return ActivityLevel.LIGHT;
        }
        if (lower.contains("o'rtacha") || lower.contains("умеренная") || lower.contains("moderate") || lower.contains("🏃")) {
            return ActivityLevel.MODERATE;
        }
        if (lower.contains("juda") || lower.contains("очень") || lower.contains("very") || lower.contains("⚡")) {
            return ActivityLevel.VERY_ACTIVE;
        }
        if (lower.contains("faol") || lower.contains("активный") || lower.contains("active") || lower.contains("🏋")) {
            return ActivityLevel.ACTIVE;
        }
        return null;
    }

    private Goal parseGoal(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();
        if (lower.contains("yo'qotish") || lower.contains("похудение") || lower.contains("lose") || lower.contains("📉")) {
            return Goal.LOSE_WEIGHT;
        }
        if (lower.contains("saqlash") || lower.contains("поддержание") || lower.contains("maintain") || lower.contains("⚖")) {
            return Goal.MAINTAIN;
        }
        if (lower.contains("olish") || lower.contains("набор") || lower.contains("gain") || lower.contains("📈")) {
            return Goal.GAIN_WEIGHT;
        }
        return null;
    }
}
