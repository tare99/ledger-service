package io.github.tare99.ledgerservice.domain.repository;

import io.github.tare99.ledgerservice.domain.entity.Transaction;

public interface CustomTransactionRepository {
  void insert(Transaction transaction);
}