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
import io.github.tare99.paymentprocessor.api.response.RefundPaymentResponse;
import io.github.tare99.paymentprocessor.domain.entity.EntryType;
import io.github.tare99.paymentprocessor.domain.entity.LedgerEntry;
import io.github.tare99.paymentprocessor.domain.exception.AccountNotFoundException;
import io.github.tare99.paymentprocessor.domain.exception.CurrencyMismatchException;
import io.github.tare99.paymentprocessor.domain.exception.PaymentNotFoundException;
import io.github.tare99.paymentprocessor.domain.exception.UnauthorizedPaymentAccessException;
import io.github.tare99.paymentprocessor.domain.repository.LedgerEntryRepository;
import io.github.tare99.paymentprocessor.domain.service.PaymentService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PaymentServiceIT extends BaseIT {

  private static final String ALICE = "ACC-ALICE00000000001";
  private static final String BOB = "ACC-BOB000000000002";
  private static final String CAROL = "ACC-CAROL00000000003";

  @Autowired private PaymentService paymentService;
  @Autowired private LedgerEntryRepository ledgerEntryRepository;

  @Test
  void createPaymentSucceeds() {
    var request = paymentRequest(ALICE, BOB, "50.00");

    CreatePaymentResponse response = paymentService.createPayment(request, ALICE);

    assertThat(response.paymentId()).isNotBlank();
    assertThat(response.senderAccountId()).isEqualTo(ALICE);
    assertThat(response.receiverAccountId()).isEqualTo(BOB);
    assertThat(response.amount()).isEqualByComparingTo("50.00");
    assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED);
  }

  @Test
  void createPaymentFromOtherAccountThrows() {
    var request = paymentRequest(ALICE, BOB, "50.00");

    assertThatThrownBy(() -> paymentService.createPayment(request, BOB))
        .isInstanceOf(UnauthorizedPaymentAccessException.class)
        .hasMessageContaining("your own account");
  }

  @Test
  void createPaymentIdempotencyReturnsSamePayment() {
    String requestId = UUID.randomUUID().toString();
    var request =
        new CreatePaymentRequest(requestId, ALICE, BOB, new BigDecimal("25.00"), Currency.USD);

    CreatePaymentResponse first = paymentService.createPayment(request, ALICE);
    CreatePaymentResponse second = paymentService.createPayment(request, ALICE);

    assertThat(first.paymentId()).isEqualTo(second.paymentId());
  }

  @Test
  void createPaymentWithSameAccountThrows() {
    var request = paymentRequest(ALICE, ALICE, "10.00");

    assertThatThrownBy(() -> paymentService.createPayment(request, ALICE))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void createPaymentWithNonexistentAccountThrows() {
    var request = paymentRequest("ACC-NONEXISTENT", BOB, "10.00");

    assertThatThrownBy(() -> paymentService.createPayment(request, "ACC-NONEXISTENT"))
        .isInstanceOf(AccountNotFoundException.class);
  }

  @Test
  void getPaymentReturnsCreatedPayment() {
    var request = paymentRequest(ALICE, BOB, "30.00");
    CreatePaymentResponse created = paymentService.createPayment(request, ALICE);

    PaymentResponse response = paymentService.getPayment(created.paymentId(), ALICE);

    assertThat(response.senderAccountId()).isEqualTo(ALICE);
    assertThat(response.receiverAccountId()).isEqualTo(BOB);
    assertThat(response.amount()).isEqualByComparingTo("30.00");
  }

  @Test
  void getPaymentAsReceiverSucceeds() {
    var request = paymentRequest(ALICE, BOB, "30.00");
    CreatePaymentResponse created = paymentService.createPayment(request, ALICE);

    PaymentResponse response = paymentService.getPayment(created.paymentId(), BOB);

    assertThat(response.paymentId()).isEqualTo(created.paymentId());
  }

  @Test
  void getPaymentAsUnrelatedAccountThrows() {
    var request = paymentRequest(ALICE, BOB, "30.00");
    CreatePaymentResponse created = paymentService.createPayment(request, ALICE);

    assertThatThrownBy(
            () -> paymentService.getPayment(created.paymentId(), "ACC-CAROL00000000003"))
        .isInstanceOf(UnauthorizedPaymentAccessException.class);
  }

  @Test
  void getPaymentNotFoundThrows() {
    assertThatThrownBy(() -> paymentService.getPayment("PAY-NONEXISTENT", ALICE))
        .isInstanceOf(PaymentNotFoundException.class);
  }

  @Test
  void getPaymentStatusReturnsCorrectStatus() {
    var request = paymentRequest(ALICE, BOB, "15.00");
    CreatePaymentResponse created = paymentService.createPayment(request, ALICE);

    PaymentStatusResponse status = paymentService.getPaymentStatus(created.paymentId(), ALICE);

    assertThat(status.status()).isEqualTo(PaymentStatus.COMPLETED);
  }

  @Test
  void listPaymentsReturnsResults() {
    var request = paymentRequest(ALICE, BOB, "5.00");
    paymentService.createPayment(request, ALICE);

    PaginatedPaymentResponse response =
        paymentService.listPayments(null, null, null, 0, 10, ALICE);

    assertThat(response.payments()).isNotEmpty();
    assertThat(response.pagination().page()).isZero();
  }

  @Test
  void listPaymentsOnlyShowsOwnPayments() {
    var request = paymentRequest(ALICE, BOB, "5.00");
    paymentService.createPayment(request, ALICE);

    PaginatedPaymentResponse response =
        paymentService.listPayments(null, null, null, 0, 10, "ACC-CAROL00000000003");

    assertThat(response.payments()).isEmpty();
  }

  @Test
  void listPaymentsFiltersBySender() {
    var request = paymentRequest(ALICE, BOB, "5.00");
    paymentService.createPayment(request, ALICE);

    PaginatedPaymentResponse response =
        paymentService.listPayments(ALICE, null, null, 0, 10, ALICE);

    assertThat(response.payments()).isNotEmpty();
    assertThat(response.payments())
        .allMatch(p -> p.senderAccountId().equals(ALICE));
  }

  @Test
  void createPaymentWritesTwoLedgerEntries() {
    var request = paymentRequest(ALICE, BOB, "100.00");

    CreatePaymentResponse response = paymentService.createPayment(request, ALICE);

    List<LedgerEntry> entries =
        ledgerEntryRepository.findByPaymentPaymentIdOrderByIdAsc(response.paymentId());
    assertThat(entries).hasSize(2);

    LedgerEntry debit = entries.get(0);
    assertThat(debit.getEntryType()).isEqualTo(EntryType.DEBIT);
    assertThat(debit.getAmount()).isEqualByComparingTo("100.00");

    LedgerEntry credit = entries.get(1);
    assertThat(credit.getEntryType()).isEqualTo(EntryType.CREDIT);
    assertThat(credit.getAmount()).isEqualByComparingTo("100.00");
  }

  @Test
  void idempotentPaymentDoesNotDuplicateLedgerEntries() {
    String requestId = UUID.randomUUID().toString();
    var request =
        new CreatePaymentRequest(requestId, ALICE, BOB, new BigDecimal("20.00"), Currency.USD);

    CreatePaymentResponse first = paymentService.createPayment(request, ALICE);
    paymentService.createPayment(request, ALICE);

    List<LedgerEntry> entries =
        ledgerEntryRepository.findByPaymentPaymentIdOrderByIdAsc(first.paymentId());
    assertThat(entries).hasSize(2);
  }

  @Test
  void refundCompletedPaymentSucceeds() {
    var request = paymentRequest(ALICE, BOB, "10.00");
    CreatePaymentResponse created = paymentService.createPayment(request, ALICE);

    RefundPaymentResponse refund = paymentService.refundPayment(created.paymentId(), ALICE);

    assertThat(refund.paymentId()).isEqualTo(created.paymentId());
    assertThat(refund.refundedAmount()).isEqualByComparingTo("10.00");
    assertThat(refund.status()).isEqualTo(PaymentStatus.REFUNDED);
  }

  @Test
  void refundByNonSenderThrows() {
    var request = paymentRequest(ALICE, BOB, "10.00");
    CreatePaymentResponse created = paymentService.createPayment(request, ALICE);

    assertThatThrownBy(() -> paymentService.refundPayment(created.paymentId(), BOB))
        .isInstanceOf(UnauthorizedPaymentAccessException.class)
        .hasMessageContaining("original sender");
  }

  @Test
  void refundNonCompletedPaymentThrows() {
    var request = paymentRequest(ALICE, BOB, "10.00");
    CreatePaymentResponse created = paymentService.createPayment(request, ALICE);
    paymentService.refundPayment(created.paymentId(), ALICE);

    assertThatThrownBy(() -> paymentService.refundPayment(created.paymentId(), ALICE))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void createPaymentWithWrongRequestCurrencyThrows() {
    var request =
        new CreatePaymentRequest(
            UUID.randomUUID().toString(), ALICE, BOB, new BigDecimal("10.00"), Currency.EUR);

    assertThatThrownBy(() -> paymentService.createPayment(request, ALICE))
        .isInstanceOf(CurrencyMismatchException.class)
        .hasMessageContaining("USD")
        .hasMessageContaining("EUR");
  }

  @Test
  void createPaymentBetweenDifferentCurrencyAccountsThrows() {
    var request =
        new CreatePaymentRequest(
            UUID.randomUUID().toString(), ALICE, CAROL, new BigDecimal("10.00"), Currency.USD);

    assertThatThrownBy(() -> paymentService.createPayment(request, ALICE))
        .isInstanceOf(CurrencyMismatchException.class)
        .hasMessageContaining("EUR");
  }

  private CreatePaymentRequest paymentRequest(String sender, String receiver, String amount) {
    return new CreatePaymentRequest(
        UUID.randomUUID().toString(), sender, receiver, new BigDecimal(amount), Currency.USD);
  }
}