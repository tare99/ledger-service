package io.github.tare99.paymentprocessor.api.response;

import io.github.tare99.paymentprocessor.api.request.Currency;
import io.github.tare99.paymentprocessor.domain.entity.AccountType;
import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
    String accountNumber,
    BigDecimal balance,
    Currency currency,
    AccountType accountType,
    Instant createdAt) {}
