package io.github.tare99.paymentprocessor.domain.entity;

import io.github.tare99.paymentprocessor.api.request.Currency;
import io.github.tare99.paymentprocessor.domain.exception.InsufficientFundsException;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

  private BigDecimal balance;

  @Enumerated(EnumType.STRING)
  private Currency currency;

  @Enumerated(EnumType.STRING)
  private AccountType accountType;

  @CreationTimestamp private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;

  public void debit(BigDecimal amount) {
    applyEntry(EntryType.DEBIT, amount);
  }

  public void credit(BigDecimal amount) {
    applyEntry(EntryType.CREDIT, amount);
  }

  private void applyEntry(EntryType entryType, BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException(entryType + " amount must be positive");
    }
    if (accountType.increases(entryType)) {
      this.balance = this.balance.add(amount);
    } else {
      requireSufficientBalance(amount);
      this.balance = this.balance.subtract(amount);
    }
  }

  private void requireSufficientBalance(BigDecimal amount) {
    if (this.balance.compareTo(amount) < 0) {
      throw new InsufficientFundsException(
          "Insufficient funds in account "
              + accountNumber
              + ". Balance: "
              + balance
              + ", required: "
              + amount);
    }
  }
}
