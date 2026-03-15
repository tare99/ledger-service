package io.github.tare99.paymentprocessor.domain.repository;

import io.github.tare99.paymentprocessor.domain.entity.Transaction;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class CustomTransactionRepositoryImpl implements CustomTransactionRepository {

  private final EntityManager entityManager;

  public CustomTransactionRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public void insert(Transaction transaction) {
    if (transaction.getId() != null) {
      throw new IllegalArgumentException("Cannot insert a transaction that already has an id");
    }
    entityManager.persist(transaction);
  }
}