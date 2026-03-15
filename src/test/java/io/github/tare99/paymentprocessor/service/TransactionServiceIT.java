package io.github.tare99.paymentprocessor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.tare99.paymentprocessor.api.request.CreateTransactionRequest;
import io.github.tare99.paymentprocessor.api.request.CreateTransactionRequest.EntryInstruction;
import io.github.tare99.paymentprocessor.api.response.TransactionResponse;
import io.github.tare99.paymentprocessor.domain.entity.EntryType;
import io.github.tare99.paymentprocessor.domain.entity.LedgerEntry;
import io.github.tare99.paymentprocessor.domain.entity.TransactionStatus;
import io.github.tare99.paymentprocessor.domain.exception.AccountNotFoundException;
import io.github.tare99.paymentprocessor.domain.exception.CurrencyMismatchException;
import io.github.tare99.paymentprocessor.domain.exception.InsufficientFundsException;
import io.github.tare99.paymentprocessor.domain.exception.TransactionNotFoundException;
import io.github.tare99.paymentprocessor.domain.exception.UnbalancedTransactionException;
import io.github.tare99.paymentprocessor.domain.repository.LedgerEntryRepository;
import io.github.tare99.paymentprocessor.domain.service.TransactionService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TransactionServiceIT extends BaseIT {

  private static final String ALICE = "ACC-ALICE00000000001";
  private static final String BOB = "ACC-BOB000000000002";
  private static final String CAROL = "ACC-CAROL00000000003";

  @Autowired private TransactionService transactionService;
  @Autowired private LedgerEntryRepository ledgerEntryRepository;

  @Test
  void createTransactionSucceeds() {
    var request = transferRequest(ALICE, BOB, "50.00");

    TransactionResponse response = transactionService.createTransaction(request);

    assertThat(response.transactionId()).isNotBlank();
    assertThat(response.status()).isEqualTo(TransactionStatus.POSTED);
    assertThat(response.entries()).hasSize(2);
    assertThat(response.entries().get(0).accountNumber()).isEqualTo(ALICE);
    assertThat(response.entries().get(0).entryType()).isEqualTo(EntryType.DEBIT);
    assertThat(response.entries().get(0).amount()).isEqualByComparingTo("50.00");
    assertThat(response.entries().get(1).accountNumber()).isEqualTo(BOB);
    assertThat(response.entries().get(1).entryType()).isEqualTo(EntryType.CREDIT);
  }

  @Test
  void createTransactionIdempotency() {
    String key = UUID.randomUUID().toString();
    var request =
        new CreateTransactionRequest(
            key,
            "test",
            List.of(
                new EntryInstruction(ALICE, EntryType.DEBIT, new BigDecimal("25.00")),
                new EntryInstruction(BOB, EntryType.CREDIT, new BigDecimal("25.00"))));

    TransactionResponse first = transactionService.createTransaction(request);
    TransactionResponse second = transactionService.createTransaction(request);

    assertThat(first.transactionId()).isEqualTo(second.transactionId());
  }

  @Test
  void createUnbalancedTransactionThrows() {
    var request =
        new CreateTransactionRequest(
            UUID.randomUUID().toString(),
            "unbalanced",
            List.of(
                new EntryInstruction(ALICE, EntryType.DEBIT, new BigDecimal("100.00")),
                new EntryInstruction(BOB, EntryType.CREDIT, new BigDecimal("50.00"))));

    assertThatThrownBy(() -> transactionService.createTransaction(request))
        .isInstanceOf(UnbalancedTransactionException.class)
        .hasMessageContaining("100.00")
        .hasMessageContaining("50.00");
  }

  @Test
  void createTransactionWithNonexistentAccountThrows() {
    var request =
        new CreateTransactionRequest(
            UUID.randomUUID().toString(),
            "bad account",
            List.of(
                new EntryInstruction("ACC-NONEXISTENT", EntryType.DEBIT, new BigDecimal("50.00")),
                new EntryInstruction(BOB, EntryType.CREDIT, new BigDecimal("50.00"))));

    assertThatThrownBy(() -> transactionService.createTransaction(request))
        .isInstanceOf(AccountNotFoundException.class);
  }

  @Test
  void createTransactionWithDifferentCurrenciesThrows() {
    var request =
        new CreateTransactionRequest(
            UUID.randomUUID().toString(),
            "cross currency",
            List.of(
                new EntryInstruction(ALICE, EntryType.DEBIT, new BigDecimal("50.00")),
                new EntryInstruction(CAROL, EntryType.CREDIT, new BigDecimal("50.00"))));

    assertThatThrownBy(() -> transactionService.createTransaction(request))
        .isInstanceOf(CurrencyMismatchException.class);
  }

  @Test
  void createTransactionWithInsufficientFundsThrows() {
    var request = transferRequest(ALICE, BOB, "999999.00");

    assertThatThrownBy(() -> transactionService.createTransaction(request))
        .isInstanceOf(InsufficientFundsException.class);
  }

  @Test
  void getTransactionReturnsCreated() {
    var request = transferRequest(ALICE, BOB, "30.00");
    TransactionResponse created = transactionService.createTransaction(request);

    TransactionResponse fetched = transactionService.getTransaction(created.transactionId());

    assertThat(fetched.transactionId()).isEqualTo(created.transactionId());
    assertThat(fetched.entries()).hasSize(2);
  }

  @Test
  void getTransactionNotFoundThrows() {
    assertThatThrownBy(() -> transactionService.getTransaction("TXN-NONEXISTENT"))
        .isInstanceOf(TransactionNotFoundException.class);
  }

  @Test
  void reverseTransactionSucceeds() {
    var request = transferRequest(ALICE, BOB, "100.00");
    TransactionResponse created = transactionService.createTransaction(request);

    TransactionResponse reversed = transactionService.reverseTransaction(created.transactionId());

    assertThat(reversed.status()).isEqualTo(TransactionStatus.REVERSED);
    assertThat(reversed.entries()).hasSize(4);
  }

  @Test
  void reverseAlreadyReversedTransactionReturnsExisting() {
    var request = transferRequest(ALICE, BOB, "100.00");
    TransactionResponse created = transactionService.createTransaction(request);

    TransactionResponse first = transactionService.reverseTransaction(created.transactionId());
    TransactionResponse second = transactionService.reverseTransaction(created.transactionId());

    assertThat(first.transactionId()).isEqualTo(second.transactionId());
    assertThat(second.status()).isEqualTo(TransactionStatus.REVERSED);
  }

  @Test
  void createTransactionWritesLedgerEntries() {
    var request = transferRequest(ALICE, BOB, "75.00");

    TransactionResponse response = transactionService.createTransaction(request);

    List<LedgerEntry> entries =
        ledgerEntryRepository.findByTransactionTransactionIdOrderByIdAsc(response.transactionId());
    assertThat(entries).hasSize(2);

    LedgerEntry debit = entries.get(0);
    assertThat(debit.getEntryType()).isEqualTo(EntryType.DEBIT);
    assertThat(debit.getAmount()).isEqualByComparingTo("75.00");

    LedgerEntry credit = entries.get(1);
    assertThat(credit.getEntryType()).isEqualTo(EntryType.CREDIT);
    assertThat(credit.getAmount()).isEqualByComparingTo("75.00");
  }

  @Test
  void idempotentTransactionDoesNotDuplicateLedgerEntries() {
    String key = UUID.randomUUID().toString();
    var request =
        new CreateTransactionRequest(
            key,
            "test",
            List.of(
                new EntryInstruction(ALICE, EntryType.DEBIT, new BigDecimal("20.00")),
                new EntryInstruction(BOB, EntryType.CREDIT, new BigDecimal("20.00"))));

    TransactionResponse first = transactionService.createTransaction(request);
    transactionService.createTransaction(request);

    List<LedgerEntry> entries =
        ledgerEntryRepository.findByTransactionTransactionIdOrderByIdAsc(first.transactionId());
    assertThat(entries).hasSize(2);
  }

  @Test
  void createTransactionWithSameAccountSucceeds() {
    var request =
        new CreateTransactionRequest(
            UUID.randomUUID().toString(),
            "self transfer",
            List.of(
                new EntryInstruction(ALICE, EntryType.DEBIT, new BigDecimal("50.00")),
                new EntryInstruction(ALICE, EntryType.CREDIT, new BigDecimal("50.00"))));

    TransactionResponse response = transactionService.createTransaction(request);
    assertThat(response.status()).isEqualTo(TransactionStatus.POSTED);
  }

  private CreateTransactionRequest transferRequest(String from, String to, String amount) {
    return new CreateTransactionRequest(
        UUID.randomUUID().toString(),
        null,
        List.of(
            new EntryInstruction(from, EntryType.DEBIT, new BigDecimal(amount)),
            new EntryInstruction(to, EntryType.CREDIT, new BigDecimal(amount))));
  }
}
