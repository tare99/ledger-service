package io.github.tare99.paymentprocessor.entity;

import com.github.f4b6a3.ulid.UlidCreator;
import io.github.tare99.paymentprocessor.api.request.Currency;
import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import jakarta.persistence.Entity;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String paymentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_account_id")
  private Account senderAccount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_account_id")
  private Account receiverAccount;

  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  private Currency currency;

  @Enumerated(EnumType.STRING)
  private PaymentStatus status;

  private String description;

  private String idempotencyKey;

  private Double riskScore;

  private String clientIp;

  @CreationTimestamp private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;

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

  private static String generatePaymentId() {
    return UlidCreator.getUlid().toString();
  }
}
