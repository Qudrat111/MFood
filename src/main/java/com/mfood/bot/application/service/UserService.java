package com.mfood.bot.application.service;

import com.mfood.bot.domain.enums.Language;
import com.mfood.bot.domain.enums.UserState;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User getOrCreateUser(Long telegramId, String username, String firstName, String lastName) {
        return userRepository.findByTelegramId(telegramId)
                .orElseGet(() -> {
                    log.info("Creating new user for telegramId={}", telegramId);
                    User user = User.builder()
                            .telegramId(telegramId)
                            .username(username)
                            .firstName(firstName)
                            .lastName(lastName)
                            .state(UserState.AWAITING_LANGUAGE)
                            .build();
                    return userRepository.save(user);
                });
    }

    @Transactional
    public User updatePhone(Long telegramId, String phone) {
        User user = getByTelegramId(telegramId);
        user.setPhoneNumber(phone);
        return userRepository.save(user);
    }

    @Transactional
    public User updateLanguage(Long telegramId, Language language) {
        User user = getByTelegramId(telegramId);
        user.setLanguage(language);
        return userRepository.save(user);
    }

    @Transactional
    public User updateState(Long telegramId, UserState state) {
        User user = getByTelegramId(telegramId);
        user.setState(state);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    private User getByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException("User not found for telegramId=" + telegramId));
    }
}
