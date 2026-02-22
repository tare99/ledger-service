package io.github.tare99.paymentprocessor.api.response;

import io.github.tare99.paymentprocessor.api.request.Currency;
import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
    String transactionId,
    String senderAccountId,
    String receiverAccountId,
    BigDecimal amount,
    Currency currency,
    PaymentStatus status,
    Instant createdAt,
    Instant updatedAt) {}
