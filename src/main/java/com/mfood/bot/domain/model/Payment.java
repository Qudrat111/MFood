package com.mfood.bot.domain.model;

import com.mfood.bot.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "telegram_charge_id", unique = true)
    private String telegramChargeId;

    @Column(name = "provider_charge_id")
    private String providerChargeId;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "UZS";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "invoice_payload", length = 500)
    private String invoicePayload;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
