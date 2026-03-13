package io.github.tare99.paymentprocessor.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "api_key")
@Getter
@NoArgsConstructor
public class ApiKey {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String keyHash;

  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  private boolean active;

  @CreationTimestamp private Instant createdAt;

  private Instant lastUsedAt;

  public ApiKey(String keyHash, String name, Account account) {
    this.keyHash = keyHash;
    this.name = name;
    this.account = account;
    this.active = true;
  }

  public void recordUsage() {
    this.lastUsedAt = Instant.now();
  }
}
