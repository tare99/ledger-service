package io.github.tare99.ledgerservice.api.response;

import io.github.tare99.ledgerservice.domain.entity.EntryType;
import java.math.BigDecimal;

public record LedgerEntryResponse(
    String accountNumber,
    EntryType entryType,
    BigDecimal amount,
    BigDecimal balanceAfter) {}