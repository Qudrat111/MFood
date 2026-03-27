package com.mfood.bot.domain.repository;

import com.mfood.bot.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTelegramChargeId(String telegramChargeId);
    List<Payment> findByUserId(Long userId);
}
