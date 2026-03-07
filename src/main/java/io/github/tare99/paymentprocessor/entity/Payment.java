package io.github.tare99.paymentprocessor.entity;

import com.github.f4b6a3.ulid.UlidCreator;
import io.github.tare99.paymentprocessor.api.request.Currency;
import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "payment")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "payment_reference", unique = true, nullable = false, length = 30)
  private String paymentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_account_id", nullable = false)
  private Account senderAccount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_account_id", nullable = false)
  private Account receiverAccount;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private Currency currency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentStatus status;

  @Column() private String description;

  @Column(name = "idempotency_key", unique = true, nullable = false, length = 64)
  private String idempotencyKey;

  @Column(name = "risk_score")
  private Double riskScore;

  @Column(name = "client_ip", length = 45)
  private String clientIp;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Builder
  public Payment(
      Account senderAccount,
      Account receiverAccount,
      BigDecimal amount,
      Currency currency,
      PaymentStatus status,
      String description,
      String idempotencyKey,
      Double riskScore,
      String clientIp) {
    this.paymentId = generatePaymentId();
    this.senderAccount = senderAccount;
    this.receiverAccount = receiverAccount;
    this.amount = amount;
    this.currency = currency;
    this.status = status;
    this.description = description;
    this.idempotencyKey = idempotencyKey;
    this.riskScore = riskScore;
    this.clientIp = clientIp;
  }

  public void updateStatus(PaymentStatus newStatus) {
    Set<PaymentStatus> allowed =
        switch (this.status) {
          case PENDING ->
              Set.of(PaymentStatus.COMPLETED, PaymentStatus.FAILED, PaymentStatus.CANCELLED);
          case COMPLETED, FAILED, CANCELLED -> Set.of();
        };
    if (!allowed.contains(newStatus)) {
      throw new IllegalStateException(
          "Invalid status transition: " + this.status + " → " + newStatus);
    }
    this.status = newStatus;
  }

  private static String generatePaymentId() {
    return UlidCreator.getUlid().toString();
  }
}
