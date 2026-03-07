package io.github.tare99.paymentprocessor.service;

import io.github.tare99.paymentprocessor.api.request.CreatePaymentRequest;
import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import io.github.tare99.paymentprocessor.api.response.CancelPaymentResponse;
import io.github.tare99.paymentprocessor.api.response.CreatePaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentStatusResponse;

public interface PaymentService {

  CreatePaymentResponse createPayment(CreatePaymentRequest request, String clientIp);

  PaymentResponse getPayment(String paymentId);

  PaginatedPaymentResponse listPayments(
      String senderAccountNumber,
      String receiverAccountNumber,
      PaymentStatus status,
      int page,
      int size);

  CancelPaymentResponse cancelPayment(String paymentId);

  PaymentStatusResponse getPaymentStatus(String paymentId);
}
