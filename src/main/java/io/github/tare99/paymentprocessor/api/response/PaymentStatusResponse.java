package io.github.tare99.paymentprocessor.api.response;

import io.github.tare99.paymentprocessor.api.request.PaymentStatus;

public record PaymentStatusResponse(String transactionId, PaymentStatus status) {}
