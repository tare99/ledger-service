package io.github.tare99.paymentprocessor.mapper;

import io.github.tare99.paymentprocessor.api.response.CreatePaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse.PaginationResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentResponse;
import io.github.tare99.paymentprocessor.entity.Payment;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

  public PaymentResponse toPaymentResponse(Payment payment) {
    return new PaymentResponse(
        payment.getId().toString(),
        payment.getSenderAccount().getAccountNumber(),
        payment.getReceiverAccount().getAccountNumber(),
        payment.getAmount(),
        payment.getCurrency(),
        payment.getStatus(),
        payment.getCreatedAt(),
        payment.getUpdatedAt());
  }

  public CreatePaymentResponse toCreatePaymentResponse(Payment payment) {
    return new CreatePaymentResponse(
        payment.getPaymentId(),
        payment.getSenderAccount().getAccountNumber(),
        payment.getReceiverAccount().getAccountNumber(),
        payment.getAmount(),
        payment.getCurrency(),
        payment.getStatus());
  }

  public PaginatedPaymentResponse toPaginatedResponse(Page<Payment> page) {
    List<PaymentResponse> payments =
        page.getContent().stream().map(this::toPaymentResponse).toList();
    PaginationResponse pagination =
        new PaginationResponse(
            page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    return new PaginatedPaymentResponse(payments, pagination);
  }
}
