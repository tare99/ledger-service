package io.github.tare99.paymentprocessor.domain.repository;

import io.github.tare99.paymentprocessor.domain.entity.Payment;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository
    extends JpaRepository<Payment, Long>,
        JpaSpecificationExecutor<Payment>,
        CustomPaymentRepository {

  Optional<Payment> findByIdempotencyKey(String idempotencyKey);

  Optional<Payment> findByPaymentId(String paymentId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Payment p WHERE p.paymentId = :paymentId")
  Optional<Payment> findByPaymentIdForUpdate(String paymentId);
}
