package io.github.tare99.paymentprocessor.service;

import io.github.tare99.paymentprocessor.api.request.CreatePaymentRequest;
import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import io.github.tare99.paymentprocessor.api.response.CreatePaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentStatusResponse;
import io.github.tare99.paymentprocessor.api.response.RefundPaymentResponse;

public interface PaymentService {

  CreatePaymentResponse createPayment(CreatePaymentRequest request);

  RefundPaymentResponse refundPayment(String paymentId);

  PaymentResponse getPayment(String paymentId);

  PaginatedPaymentResponse listPayments(
      String senderAccountNumber,
      String receiverAccountNumber,
      PaymentStatus status,
      int page,
      int size);

  PaymentStatusResponse getPaymentStatus(String paymentId);
}
