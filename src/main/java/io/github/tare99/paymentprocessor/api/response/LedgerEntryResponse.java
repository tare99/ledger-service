package io.github.tare99.paymentprocessor.api.response;

import io.github.tare99.paymentprocessor.domain.entity.EntryType;
import java.math.BigDecimal;

public record LedgerEntryResponse(
    String accountNumber,
    EntryType entryType,
    BigDecimal amount,
    BigDecimal balanceAfter) {}