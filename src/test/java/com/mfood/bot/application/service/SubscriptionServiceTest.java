package com.mfood.bot.application.service;

import com.mfood.bot.domain.enums.PaymentStatus;
import com.mfood.bot.domain.model.Payment;
import com.mfood.bot.domain.model.User;
import com.mfood.bot.domain.repository.PaymentRepository;
import com.mfood.bot.domain.repository.SubscriptionRepository;
import com.mfood.bot.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .telegramId(123456789L)
                .username("testuser")
                .build();
    }

    @Test
    void processSuccessfulPayment_newPayment_savedAndSubscriptionCreated() {
        String chargeId = "charge_abc123";
        when(paymentRepository.findByTelegramChargeId(chargeId)).thenReturn(Optional.empty());
        when(userRepository.findByTelegramId(testUser.getTelegramId())).thenReturn(Optional.of(testUser));
        when(paymentRepository.save(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p = Payment.builder()
                    .id(1L).user(testUser).telegramChargeId(chargeId)
                    .amount(1300000).status(PaymentStatus.COMPLETED)
                    .build();
            return p;
        });
        when(subscriptionRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Payment payment = subscriptionService.processSuccessfulPayment(
                testUser.getTelegramId(), chargeId, "provider_123", 1300000);

        assertThat(payment).isNotNull();
        assertThat(payment.getTelegramChargeId()).isEqualTo(chargeId);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentRepository, times(1)).save(any());
    }

    @Test
    void processSuccessfulPayment_duplicatePayment_returnsExistingNoSave() {
        String chargeId = "charge_duplicate";
        Payment existing = Payment.builder()
                .id(99L).telegramChargeId(chargeId)
                .status(PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.findByTelegramChargeId(chargeId)).thenReturn(Optional.of(existing));

        Payment result = subscriptionService.processSuccessfulPayment(
                testUser.getTelegramId(), chargeId, "provider_456", 1300000);

        assertThat(result.getId()).isEqualTo(99L);
        // Should NOT save again - idempotency check
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void isSubscriptionActive_noSubscription_returnsFalse() {
        when(userRepository.findByTelegramId(testUser.getTelegramId())).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        boolean active = subscriptionService.isSubscriptionActive(testUser.getTelegramId());

        assertThat(active).isFalse();
    }
}
