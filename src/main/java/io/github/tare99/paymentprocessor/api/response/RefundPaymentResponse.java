package io.github.tare99.paymentprocessor.api.response;

import io.github.tare99.paymentprocessor.api.request.Currency;
import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import java.math.BigDecimal;

public record RefundPaymentResponse(
    String paymentId,
    BigDecimal refundedAmount,
    Currency currency,
    PaymentStatus status) {}
