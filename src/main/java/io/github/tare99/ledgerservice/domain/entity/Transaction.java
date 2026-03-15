package io.github.tare99.ledgerservice.domain.entity;

import com.github.f4b6a3.ulid.UlidCreator;
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
@Table(name = "ledger_transaction")
@Getter
@NoArgsConstructor
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String transactionId;

  @Enumerated(EnumType.STRING)
  private TransactionStatus status;

  private String idempotencyKey;

  private String description;

  @CreationTimestamp private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;

  public Transaction(String idempotencyKey, String description) {
    this.transactionId = UlidCreator.getUlid().toString();
    this.status = TransactionStatus.POSTED;
    this.idempotencyKey = idempotencyKey;
    this.description = description;
  }

  public void reverse() {
    if (this.status != TransactionStatus.POSTED) {
      throw new IllegalStateException("Can only reverse a POSTED transaction");
    }
    this.status = TransactionStatus.REVERSED;
  }
}