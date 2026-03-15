package io.github.tare99.paymentprocessor.api.response;

import io.github.tare99.paymentprocessor.domain.entity.TransactionStatus;
import java.time.Instant;
import java.util.List;

public record TransactionResponse(
    String transactionId,
    TransactionStatus status,
    String description,
    List<LedgerEntryResponse> entries,
    Instant createdAt) {}