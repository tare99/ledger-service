package io.github.tare99.paymentprocessor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.tare99.paymentprocessor.api.request.CreatePaymentRequest;
import io.github.tare99.paymentprocessor.api.request.Currency;
import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import io.github.tare99.paymentprocessor.api.response.CreatePaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentStatusResponse;
import io.github.tare99.paymentprocessor.exception.AccountNotFoundException;
import io.github.tare99.paymentprocessor.exception.PaymentNotFoundException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PaymentServiceIT extends BaseIT {

  @Autowired private PaymentService paymentService;

  @Test
  void createPaymentSucceeds() {
    var request =
        new CreatePaymentRequest(
            UUID.randomUUID().toString(),
            "ACC-ALICE00000000001",
            "ACC-BOB000000000002",
            new BigDecimal("50.00"),
            Currency.USD);

    CreatePaymentResponse response = paymentService.createPayment(request, "127.0.0.1");

    assertThat(response.paymentId()).isNotBlank();
    assertThat(response.senderAccountId()).isEqualTo("ACC-ALICE00000000001");
    assertThat(response.receiverAccountId()).isEqualTo("ACC-BOB000000000002");
    assertThat(response.amount()).isEqualByComparingTo("50.00");
    assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED);
  }

  @Test
  void createPaymentIdempotencyReturnsSamePayment() {
    String requestId = UUID.randomUUID().toString();
    var request =
        new CreatePaymentRequest(
            requestId,
            "ACC-ALICE00000000001",
            "ACC-BOB000000000002",
            new BigDecimal("25.00"),
            Currency.USD);

    CreatePaymentResponse first = paymentService.createPayment(request, "127.0.0.1");
    CreatePaymentResponse second = paymentService.createPayment(request, "127.0.0.1");

    assertThat(first.paymentId()).isEqualTo(second.paymentId());
  }

  @Test
  void createPaymentWithSameAccountThrows() {
    var request =
        new CreatePaymentRequest(
            UUID.randomUUID().toString(),
            "ACC-ALICE00000000001",
            "ACC-ALICE00000000001",
            new BigDecimal("10.00"),
            Currency.USD);

    assertThatThrownBy(() -> paymentService.createPayment(request, "127.0.0.1"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void createPaymentWithNonexistentAccountThrows() {
    var request =
        new CreatePaymentRequest(
            UUID.randomUUID().toString(),
            "ACC-NONEXISTENT",
            "ACC-BOB000000000002",
            new BigDecimal("10.00"),
            Currency.USD);

    assertThatThrownBy(() -> paymentService.createPayment(request, "127.0.0.1"))
        .isInstanceOf(AccountNotFoundException.class);
  }

  @Test
  void getPaymentReturnsCreatedPayment() {
    var request =
        new CreatePaymentRequest(
            UUID.randomUUID().toString(),
            "ACC-ALICE00000000001",
            "ACC-BOB000000000002",
            new BigDecimal("30.00"),
            Currency.USD);
    CreatePaymentResponse created = paymentService.createPayment(request, "127.0.0.1");

    PaymentResponse response = paymentService.getPayment(created.paymentId());

    assertThat(response.senderAccountId()).isEqualTo("ACC-ALICE00000000001");
    assertThat(response.receiverAccountId()).isEqualTo("ACC-BOB000000000002");
    assertThat(response.amount()).isEqualByComparingTo("30.00");
  }

  @Test
  void getPaymentNotFoundThrows() {
    assertThatThrownBy(() -> paymentService.getPayment("PAY-NONEXISTENT"))
        .isInstanceOf(PaymentNotFoundException.class);
  }

  @Test
  void getPaymentStatusReturnsCorrectStatus() {
    var request =
        new CreatePaymentRequest(
            UUID.randomUUID().toString(),
            "ACC-ALICE00000000001",
            "ACC-BOB000000000002",
            new BigDecimal("15.00"),
            Currency.USD);
    CreatePaymentResponse created = paymentService.createPayment(request, "127.0.0.1");

    PaymentStatusResponse status = paymentService.getPaymentStatus(created.paymentId());

    assertThat(status.status()).isEqualTo(PaymentStatus.COMPLETED);
  }

  @Test
  void listPaymentsReturnsResults() {
    var request =
        new CreatePaymentRequest(
            UUID.randomUUID().toString(),
            "ACC-ALICE00000000001",
            "ACC-BOB000000000002",
            new BigDecimal("5.00"),
            Currency.USD);
    paymentService.createPayment(request, "127.0.0.1");

    PaginatedPaymentResponse response = paymentService.listPayments(null, null, null, 0, 10);

    assertThat(response.payments()).isNotEmpty();
    assertThat(response.pagination().page()).isEqualTo(0);
  }

  @Test
  void listPaymentsFiltersBySender() {
    var request =
        new CreatePaymentRequest(
            UUID.randomUUID().toString(),
            "ACC-ALICE00000000001",
            "ACC-BOB000000000002",
            new BigDecimal("5.00"),
            Currency.USD);
    paymentService.createPayment(request, "127.0.0.1");

    PaginatedPaymentResponse response =
        paymentService.listPayments("ACC-ALICE00000000001", null, null, 0, 10);

    assertThat(response.payments())
        .allMatch(p -> p.senderAccountId().equals("ACC-ALICE00000000001"));
  }

  @Test
  void cancelCompletedPaymentThrows() {
    var request =
        new CreatePaymentRequest(
            UUID.randomUUID().toString(),
            "ACC-ALICE00000000001",
            "ACC-BOB000000000002",
            new BigDecimal("10.00"),
            Currency.USD);
    CreatePaymentResponse created = paymentService.createPayment(request, "127.0.0.1");

    assertThatThrownBy(() -> paymentService.cancelPayment(created.paymentId()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("PENDING");
  }
}
