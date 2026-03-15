package io.github.tare99.ledgerservice.api.response;

import io.github.tare99.ledgerservice.domain.entity.EntryType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PaginatedEntriesResponse(
    List<EntryWithTransactionResponse> entries, PaginationResponse pagination) {

  public record EntryWithTransactionResponse(
      String transactionId,
      EntryType entryType,
      BigDecimal amount,
      BigDecimal balanceAfter,
      Instant createdAt) {}

  public record PaginationResponse(int page, int size, long totalElements, int totalPages) {}
}