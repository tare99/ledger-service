package io.github.tare99.paymentprocessor.repository;

import io.github.tare99.paymentprocessor.entity.Payment;

public interface CustomPaymentRepository {
  void insert(Payment payment);
}
