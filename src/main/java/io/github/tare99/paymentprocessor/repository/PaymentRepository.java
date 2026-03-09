package io.github.tare99.paymentprocessor.repository;

import io.github.tare99.paymentprocessor.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaymentRepository
    extends JpaRepository<Payment, Long>,
        JpaSpecificationExecutor<Payment>,
        CustomPaymentRepository {

  Optional<Payment> findByIdempotencyKey(String idempotencyKey);

  Optional<Payment> findByPaymentId(String paymentId);
}
