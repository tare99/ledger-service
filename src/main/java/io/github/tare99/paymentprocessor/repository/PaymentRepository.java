package io.github.tare99.paymentprocessor.repository;

import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import io.github.tare99.paymentprocessor.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository
    extends JpaRepository<Payment, Long>,
        JpaSpecificationExecutor<Payment>,
        CustomPaymentRepository {

  Optional<Payment> findByIdempotencyKey(String idempotencyKey);

  Optional<Payment> findByPaymentId(String paymentId);

  @Modifying
  @Query("UPDATE Payment p SET p.status = :status WHERE p.id = :id")
  void updateStatus(@Param("id") Long id, @Param("status") PaymentStatus status);
}
