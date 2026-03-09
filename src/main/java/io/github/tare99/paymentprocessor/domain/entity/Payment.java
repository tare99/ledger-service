package io.github.tare99.paymentprocessor.domain.entity;

import com.github.f4b6a3.ulid.UlidCreator;
import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
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

  @Enumerated(EnumType.STRING)
  private PaymentStatus status;

  private String idempotencyKey;

  @CreationTimestamp private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;

  public Payment(PaymentStatus status, String idempotencyKey) {
    this.paymentId = UlidCreator.getUlid().toString();
    this.status = status;
    this.idempotencyKey = idempotencyKey;
  }

  public void updateStatus(PaymentStatus status) {
    this.status = status;
  }
}
