package io.github.tare99.paymentprocessor.api.response;

import java.util.List;

public record PaginatedPaymentResponse(
    List<PaymentResponse> payments, PaginationResponse pagination) {
  public record PaginationResponse(int page, int size, long totalElements, int totalPages) {}
}
