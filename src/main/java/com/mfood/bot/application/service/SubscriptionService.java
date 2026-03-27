package com.mfood.bot.application.service;

import com.mfood.bot.domain.enums.PaymentStatus;
import com.mfood.bot.domain.enums.SubscriptionStatus;
import com.mfood.bot.domain.model.Payment;
import com.mfood.bot.domain.model.Subscription;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.domain.repository.PaymentRepository;
import com.mfood.bot.domain.repository.SubscriptionRepository;
import com.mfood.bot.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Value("${subscription.monthly-days:30}")
    private int monthlyDays;

    @Transactional(readOnly = true)
    public boolean isSubscriptionActive(Long telegramId) {
        User user = getUser(telegramId);
        return subscriptionRepository.findByUserId(user.getId())
                .map(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE
                        && sub.getExpiresAt() != null
                        && sub.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional
    public Subscription createOrRenewSubscription(Long telegramId, Payment payment) {
        User user = getUser(telegramId);
        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                .orElseGet(() -> Subscription.builder().user(user).build());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = subscription.getStatus() == SubscriptionStatus.ACTIVE
                && subscription.getExpiresAt() != null
                && subscription.getExpiresAt().isAfter(now)
                ? subscription.getExpiresAt()
                : now;

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(now);
        subscription.setExpiresAt(startTime.plusDays(monthlyDays));

        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Subscription activated/renewed for telegramId={}, expires={}", telegramId, saved.getExpiresAt());
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Subscription> getSubscription(Long telegramId) {
        User user = getUser(telegramId);
        return subscriptionRepository.findByUserId(user.getId());
    }

    @Transactional
    public Payment processSuccessfulPayment(Long telegramId, String telegramChargeId,
                                             String providerChargeId, Integer totalAmount) {
        // Idempotency: check by telegramChargeId first
        Optional<Payment> existing = paymentRepository.findByTelegramChargeId(telegramChargeId);
        if (existing.isPresent()) {
            log.info("Duplicate payment received, telegramChargeId={}", telegramChargeId);
            return existing.get();
        }

        User user = getUser(telegramId);
        Payment payment = Payment.builder()
                .user(user)
                .telegramChargeId(telegramChargeId)
                .providerChargeId(providerChargeId)
                .amount(totalAmount)
                .status(PaymentStatus.COMPLETED)
                .currency("UZS")
                .build();
        Payment saved = paymentRepository.save(payment);
        createOrRenewSubscription(telegramId, saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Subscription> getExpiringSubscriptions(int daysBeforeExpiry) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(daysBeforeExpiry);
        return subscriptionRepository.findByStatusAndExpiresAtBetween(SubscriptionStatus.ACTIVE, start, end);
    }

    private User getUser(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException("User not found for telegramId=" + telegramId));
    }
}
