package io.github.tare99.paymentprocessor.mapper;

import io.github.tare99.paymentprocessor.api.request.CreatePaymentRequest;
import io.github.tare99.paymentprocessor.api.response.CreatePaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse.PaginationResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentResponse;
import io.github.tare99.paymentprocessor.entity.EntryType;
import io.github.tare99.paymentprocessor.entity.LedgerEntry;
import io.github.tare99.paymentprocessor.entity.Payment;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

  public PaymentResponse toPaymentResponse(Payment payment, List<LedgerEntry> entries) {
    LedgerEntry debit = findFirstByType(entries, EntryType.DEBIT);
    LedgerEntry credit = findFirstByType(entries, EntryType.CREDIT);
    return new PaymentResponse(
        payment.getPaymentId(),
        debit.getAccount().getAccountNumber(),
        credit.getAccount().getAccountNumber(),
        debit.getAmount(),
        debit.getAccount().getCurrency(),
        payment.getStatus(),
        payment.getCreatedAt(),
        payment.getUpdatedAt());
  }

  public CreatePaymentResponse toCreatePaymentResponse(Payment payment, List<LedgerEntry> entries) {
    LedgerEntry debit = findFirstByType(entries, EntryType.DEBIT);
    LedgerEntry credit = findFirstByType(entries, EntryType.CREDIT);
    return new CreatePaymentResponse(
        payment.getPaymentId(),
        debit.getAccount().getAccountNumber(),
        credit.getAccount().getAccountNumber(),
        debit.getAmount(),
        debit.getAccount().getCurrency(),
        payment.getStatus());
  }

  public CreatePaymentResponse toCreatePaymentResponse(
      Payment payment, CreatePaymentRequest request) {
    return new CreatePaymentResponse(
        payment.getPaymentId(),
        request.senderAccountId(),
        request.receiverAccountId(),
        request.amount(),
        request.currency(),
        payment.getStatus());
  }

  public PaginatedPaymentResponse toPaginatedResponse(
      Page<Payment> page, Map<Long, List<LedgerEntry>> entriesByPaymentId) {
    List<PaymentResponse> payments =
        page.getContent().stream()
            .map(p -> toPaymentResponse(p, entriesByPaymentId.get(p.getId())))
            .toList();
    PaginationResponse pagination =
        new PaginationResponse(
            page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    return new PaginatedPaymentResponse(payments, pagination);
  }

  private LedgerEntry findFirstByType(List<LedgerEntry> entries, EntryType type) {
    return entries.stream()
        .filter(e -> e.getEntryType() == type)
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException("No " + type + " ledger entry found for payment"));
  }
}
