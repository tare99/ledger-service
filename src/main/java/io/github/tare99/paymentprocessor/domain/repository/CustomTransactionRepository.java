package io.github.tare99.paymentprocessor.domain.repository;

import io.github.tare99.paymentprocessor.domain.entity.Transaction;

public interface CustomTransactionRepository {
  void insert(Transaction transaction);
}