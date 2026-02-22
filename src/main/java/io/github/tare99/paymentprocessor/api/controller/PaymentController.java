package io.github.tare99.paymentprocessor.api.controller;

import io.github.tare99.paymentprocessor.api.request.CreatePaymentRequest;
import io.github.tare99.paymentprocessor.api.request.Currency;
import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import io.github.tare99.paymentprocessor.api.response.CancelPaymentResponse;
import io.github.tare99.paymentprocessor.api.response.CreatePaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse.PaginationResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentStatusResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

  @PostMapping
  public ResponseEntity<CreatePaymentResponse> createPayment(
      @RequestBody CreatePaymentRequest request) {
    var response =
        new CreatePaymentResponse(
            "txn-001",
            request.senderAccountId(),
            request.receiverAccountId(),
            request.amount(),
            request.currency(),
            PaymentStatus.PENDING);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<PaymentResponse> getPayment(@PathVariable String id) {
    var now = Instant.now();
    var response =
        new PaymentResponse(
            id,
            "sender-001",
            "receiver-001",
            new BigDecimal("100.00"),
            Currency.USD,
            PaymentStatus.COMPLETED,
            now,
            now);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<PaginatedPaymentResponse> listPayments(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    var now = Instant.now();
    var payment =
        new PaymentResponse(
            "txn-001",
            "sender-001",
            "receiver-001",
            new BigDecimal("100.00"),
            Currency.USD,
            PaymentStatus.COMPLETED,
            now,
            now);
    var response =
        new PaginatedPaymentResponse(List.of(payment), new PaginationResponse(page, size, 1L, 1));
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/cancel")
  public ResponseEntity<CancelPaymentResponse> cancelPayment(@PathVariable String id) {
    var response = new CancelPaymentResponse("Payment " + id + " cancelled successfully");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}/status")
  public ResponseEntity<PaymentStatusResponse> getPaymentStatus(@PathVariable String id) {
    var response = new PaymentStatusResponse(id, PaymentStatus.COMPLETED);
    return ResponseEntity.ok(response);
  }
}
