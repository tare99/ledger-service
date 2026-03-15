package io.github.tare99.paymentprocessor.domain.service.impl;

import io.github.tare99.paymentprocessor.api.request.CreateTransactionRequest;
import io.github.tare99.paymentprocessor.api.request.CreateTransactionRequest.EntryInstruction;
import io.github.tare99.paymentprocessor.api.request.Currency;
import io.github.tare99.paymentprocessor.api.response.LedgerEntryResponse;
import io.github.tare99.paymentprocessor.api.response.TransactionResponse;
import io.github.tare99.paymentprocessor.domain.entity.Account;
import io.github.tare99.paymentprocessor.domain.entity.EntryType;
import io.github.tare99.paymentprocessor.domain.entity.LedgerEntry;
import io.github.tare99.paymentprocessor.domain.entity.Transaction;
import io.github.tare99.paymentprocessor.domain.entity.TransactionStatus;
import io.github.tare99.paymentprocessor.domain.exception.CurrencyMismatchException;
import io.github.tare99.paymentprocessor.domain.exception.TransactionNotFoundException;
import io.github.tare99.paymentprocessor.domain.exception.UnbalancedTransactionException;
import io.github.tare99.paymentprocessor.domain.repository.LedgerEntryRepository;
import io.github.tare99.paymentprocessor.domain.repository.TransactionRepository;
import io.github.tare99.paymentprocessor.domain.service.AccountService;
import io.github.tare99.paymentprocessor.domain.service.TransactionService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {

  private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

  private final TransactionRepository transactionRepository;
  private final AccountService accountService;
  private final LedgerEntryRepository ledgerEntryRepository;

  public TransactionServiceImpl(
      TransactionRepository transactionRepository,
      AccountService accountService,
      LedgerEntryRepository ledgerEntryRepository) {
    this.transactionRepository = transactionRepository;
    this.accountService = accountService;
    this.ledgerEntryRepository = ledgerEntryRepository;
  }

  @Override
  @Transactional
  public TransactionResponse createTransaction(CreateTransactionRequest request) {
    Optional<Transaction> optionalTransaction =
        transactionRepository.findByIdempotencyKey(request.idempotencyKey());
    if (optionalTransaction.isPresent()) {
      log.info("Transaction with idempotency key {} already exists", request.idempotencyKey());
      Transaction transaction = optionalTransaction.get();
      List<LedgerEntry> entries = ledgerEntryRepository.findEntriesWithAccount(transaction.getId());
      return toResponse(transaction, entries);
    }

    validateBalanced(request.entries());

    Set<String> accountNumbers =
        request.entries().stream().map(EntryInstruction::accountNumber).collect(Collectors.toSet());
    List<Account> accounts = accountService.getAccountsForUpdate(accountNumbers);

    validateSameCurrency(accounts);

    Transaction transaction = new Transaction(request.idempotencyKey(), request.description());
    transactionRepository.insert(transaction);

    Map<String, Account> accountMap =
        accounts.stream().collect(Collectors.toMap(Account::getAccountNumber, a -> a));
    List<LedgerEntry> entries = new ArrayList<>();
    for (EntryInstruction instruction : request.entries()) {
      Account account = accountMap.get(instruction.accountNumber());
      if (instruction.entryType() == EntryType.DEBIT) {
        account.debit(instruction.amount());
      } else {
        account.credit(instruction.amount());
      }
      entries.add(
          new LedgerEntry(
              transaction,
              account,
              instruction.entryType(),
              instruction.amount(),
              account.getBalance()));
    }
    ledgerEntryRepository.saveAll(entries);

    log.info("Transaction created, transactionId {}", transaction.getTransactionId());
    return toResponse(transaction, entries);
  }

  @Override
  @Transactional(readOnly = true)
  public TransactionResponse getTransaction(String transactionId) {
    Transaction transaction =
        transactionRepository
            .findByTransactionId(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    List<LedgerEntry> entries = ledgerEntryRepository.findEntriesWithAccount(transaction.getId());
    return toResponse(transaction, entries);
  }

  @Override
  @Transactional
  public TransactionResponse reverseTransaction(String transactionId) {
    Transaction transaction =
        transactionRepository
            .findByTransactionIdForUpdate(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

    if (transaction.getStatus() == TransactionStatus.REVERSED) {
      log.info("Transaction {} already reversed, returning existing", transactionId);
      List<LedgerEntry> entries = ledgerEntryRepository.findEntriesWithAccount(transaction.getId());
      return toResponse(transaction, entries);
    }

    List<LedgerEntry> originalEntries =
        ledgerEntryRepository.findEntriesWithAccount(transaction.getId());

    Set<String> accountNumbers =
        originalEntries.stream()
            .map(e -> e.getAccount().getAccountNumber())
            .collect(Collectors.toSet());
    List<Account> accounts = accountService.getAccountsForUpdate(accountNumbers);
    Map<String, Account> accountMap =
        accounts.stream().collect(Collectors.toMap(Account::getAccountNumber, a -> a));

    List<LedgerEntry> reversalEntries = new ArrayList<>();
    for (LedgerEntry original : originalEntries) {
      Account account = accountMap.get(original.getAccount().getAccountNumber());
      EntryType reversedType =
          original.getEntryType() == EntryType.DEBIT ? EntryType.CREDIT : EntryType.DEBIT;
      if (reversedType == EntryType.DEBIT) {
        account.debit(original.getAmount());
      } else {
        account.credit(original.getAmount());
      }
      reversalEntries.add(
          new LedgerEntry(
              transaction, account, reversedType, original.getAmount(), account.getBalance()));
    }
    ledgerEntryRepository.saveAll(reversalEntries);
    transaction.reverse();

    log.info("Transaction reversed: transactionId={}", transactionId);

    List<LedgerEntry> allEntries = new ArrayList<>(originalEntries);
    allEntries.addAll(reversalEntries);
    return toResponse(transaction, allEntries);
  }

  private void validateBalanced(List<EntryInstruction> instructions) {
    BigDecimal totalDebits =
        instructions.stream()
            .filter(e -> e.entryType() == EntryType.DEBIT)
            .map(EntryInstruction::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalCredits =
        instructions.stream()
            .filter(e -> e.entryType() == EntryType.CREDIT)
            .map(EntryInstruction::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (totalDebits.compareTo(totalCredits) != 0) {
      throw new UnbalancedTransactionException(
          "Total debits (" + totalDebits + ") must equal total credits (" + totalCredits + ")");
    }
  }

  private void validateSameCurrency(List<Account> accounts) {
    Currency currency = accounts.getFirst().getCurrency();
    for (Account account : accounts) {
      if (account.getCurrency() != currency) {
        throw new CurrencyMismatchException(
            "All accounts in a transaction must share the same currency. Found: "
                + currency
                + " and "
                + account.getCurrency());
      }
    }
  }

  private TransactionResponse toResponse(Transaction transaction, List<LedgerEntry> entries) {
    List<LedgerEntryResponse> entryResponses =
        entries.stream()
            .map(
                e ->
                    new LedgerEntryResponse(
                        e.getAccount().getAccountNumber(),
                        e.getEntryType(),
                        e.getAmount(),
                        e.getBalanceAfter()))
            .toList();
    return new TransactionResponse(
        transaction.getTransactionId(),
        transaction.getStatus(),
        transaction.getDescription(),
        entryResponses,
        transaction.getCreatedAt());
  }
}
