package io.github.tare99.paymentprocessor.domain.repository;

import io.github.tare99.paymentprocessor.domain.entity.Payment;

public interface CustomPaymentRepository {
  void insert(Payment payment);
}
