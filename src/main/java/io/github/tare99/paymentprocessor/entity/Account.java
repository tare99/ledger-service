package io.github.tare99.paymentprocessor.entity;

import io.github.tare99.paymentprocessor.api.request.Currency;
import io.github.tare99.paymentprocessor.exception.InsufficientFundsException;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "account")
@Getter
@NoArgsConstructor
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String accountNumber;

  private String ownerName;

  private String email;

  private BigDecimal balance;

  @Enumerated(EnumType.STRING)
  private Currency currency;

  @Enumerated(EnumType.STRING)
  private AccountStatus status;

  @CreationTimestamp private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;

  @Version private Long version;

  public void debit(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Debit amount must be positive");
    }
    if (this.balance.compareTo(amount) < 0) {
      throw new InsufficientFundsException(
          "Insufficient funds in account "
              + accountNumber
              + ". Balance: "
              + balance
              + ", required: "
              + amount);
    }
    this.balance = this.balance.subtract(amount);
  }

  public void credit(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Credit amount must be positive");
    }
    this.balance = this.balance.add(amount);
  }
}
