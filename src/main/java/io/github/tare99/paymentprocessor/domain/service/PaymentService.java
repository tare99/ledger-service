package io.github.tare99.paymentprocessor.domain.service;

import io.github.tare99.paymentprocessor.api.request.CreatePaymentRequest;
import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import io.github.tare99.paymentprocessor.api.response.CreatePaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentStatusResponse;
import io.github.tare99.paymentprocessor.api.response.RefundPaymentResponse;

public interface PaymentService {

  CreatePaymentResponse createPayment(CreatePaymentRequest request, String authenticatedAccountNumber);

  RefundPaymentResponse refundPayment(String paymentId, String authenticatedAccountNumber);

  PaymentResponse getPayment(String paymentId, String authenticatedAccountNumber);

  PaginatedPaymentResponse listPayments(
      String senderAccountNumber,
      String receiverAccountNumber,
      PaymentStatus status,
      int page,
      int size,
      String authenticatedAccountNumber);

  PaymentStatusResponse getPaymentStatus(String paymentId, String authenticatedAccountNumber);
}