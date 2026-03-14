package io.github.tare99.paymentprocessor.api.controller;

import io.github.tare99.paymentprocessor.api.request.CreatePaymentRequest;
import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import io.github.tare99.paymentprocessor.api.response.CreatePaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentStatusResponse;
import io.github.tare99.paymentprocessor.api.response.RefundPaymentResponse;
import io.github.tare99.paymentprocessor.domain.service.PaymentService;
import io.github.tare99.paymentprocessor.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @PostMapping
  public ResponseEntity<CreatePaymentResponse> createPayment(
      @Valid @RequestBody CreatePaymentRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(paymentService.createPayment(request, getAuthenticatedAccountNumber()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<PaymentResponse> getPayment(@PathVariable String id) {
    return ResponseEntity.ok(paymentService.getPayment(id, getAuthenticatedAccountNumber()));
  }

  @GetMapping
  public ResponseEntity<PaginatedPaymentResponse> listPayments(
      @RequestParam(required = false) String senderAccountId,
      @RequestParam(required = false) String receiverAccountId,
      @RequestParam(required = false) PaymentStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        paymentService.listPayments(
            senderAccountId,
            receiverAccountId,
            status,
            page,
            size,
            getAuthenticatedAccountNumber()));
  }

  @PostMapping("/{id}/refund")
  public ResponseEntity<RefundPaymentResponse> refundPayment(@PathVariable String id) {
    return ResponseEntity.ok(paymentService.refundPayment(id, getAuthenticatedAccountNumber()));
  }

  @GetMapping("/{id}/status")
  public ResponseEntity<PaymentStatusResponse> getPaymentStatus(@PathVariable String id) {
    return ResponseEntity.ok(paymentService.getPaymentStatus(id, getAuthenticatedAccountNumber()));
  }

  private String getAuthenticatedAccountNumber() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    var principal = (UserPrincipal) authentication.getPrincipal();
    return principal.userId();
  }
}
