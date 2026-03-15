package io.github.tare99.ledgerservice.api.response;

import io.github.tare99.ledgerservice.api.request.Currency;
import io.github.tare99.ledgerservice.domain.entity.AccountType;
import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
    String accountNumber,
    BigDecimal balance,
    Currency currency,
    AccountType accountType,
    Instant createdAt) {}
