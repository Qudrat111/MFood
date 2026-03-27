package com.mfood.bot.domain.repository;

import com.mfood.bot.domain.enums.SubscriptionStatus;
import com.mfood.bot.domain.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserId(Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.expiresAt BETWEEN :start AND :end")
    List<Subscription> findByStatusAndExpiresAtBetween(
            @Param("status") SubscriptionStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
