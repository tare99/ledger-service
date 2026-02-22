package io.github.tare99.paymentprocessor.api.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreatePaymentRequest(
    @NotNull String requestId,
    @NotNull String senderAccountId,
    @NotNull String receiverAccountId,
    @NotNull BigDecimal amount,
    @NotNull Currency currency) {}
