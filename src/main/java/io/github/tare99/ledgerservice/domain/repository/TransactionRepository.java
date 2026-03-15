package io.github.tare99.ledgerservice.domain.repository;

import io.github.tare99.ledgerservice.domain.entity.Transaction;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository
    extends JpaRepository<Transaction, Long>, CustomTransactionRepository {

  Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

  Optional<Transaction> findByTransactionId(String transactionId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT t FROM Transaction t WHERE t.transactionId = :transactionId")
  Optional<Transaction> findByTransactionIdForUpdate(String transactionId);
}
