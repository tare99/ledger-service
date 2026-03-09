package io.github.tare99.paymentprocessor.domain.repository;

import io.github.tare99.paymentprocessor.domain.entity.Payment;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class CustomPaymentRepositoryImpl implements CustomPaymentRepository {

  private final EntityManager entityManager;

  public CustomPaymentRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public void insert(Payment payment) {
    if (payment.getId() != null) {
      throw new IllegalArgumentException("Cannot insert a payment that already has an id");
    }
    entityManager.persist(payment);
  }
}
