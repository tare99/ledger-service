package io.github.tare99.paymentprocessor.domain.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "ledger_entry")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class LedgerEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", nullable = false)
  private Payment payment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @Enumerated(EnumType.STRING)
  private EntryType entryType;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  private BigDecimal balanceAfter;

  @CreatedDate private Instant createdAt;

  public LedgerEntry(
      Payment payment,
      Account account,
      EntryType entryType,
      BigDecimal amount,
      BigDecimal balanceAfter) {
    this.payment = payment;
    this.account = account;
    this.entryType = entryType;
    this.amount = amount;
    this.balanceAfter = balanceAfter;
  }
}
