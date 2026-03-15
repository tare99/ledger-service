package io.github.tare99.paymentprocessor.api.request;

import io.github.tare99.paymentprocessor.domain.entity.EntryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CreateTransactionRequest(
    @NotBlank String idempotencyKey,
    String description,
    @NotNull @Size(min = 2) @Valid List<EntryInstruction> entries) {

  public record EntryInstruction(
      @NotBlank String accountNumber,
      @NotNull EntryType entryType,
      @NotNull @Positive BigDecimal amount) {}
}
