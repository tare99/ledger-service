package io.github.tare99.ledgerservice.api.controller;

import io.github.tare99.ledgerservice.api.response.AccountResponse;
import io.github.tare99.ledgerservice.api.response.PaginatedEntriesResponse;
import io.github.tare99.ledgerservice.api.response.PaginatedEntriesResponse.EntryWithTransactionResponse;
import io.github.tare99.ledgerservice.api.response.PaginatedEntriesResponse.PaginationResponse;
import io.github.tare99.ledgerservice.domain.entity.Account;
import io.github.tare99.ledgerservice.domain.repository.LedgerEntryRepository;
import io.github.tare99.ledgerservice.domain.service.AccountService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

  private final AccountService accountService;
  private final LedgerEntryRepository ledgerEntryRepository;

  public AccountController(
      AccountService accountService, LedgerEntryRepository ledgerEntryRepository) {
    this.accountService = accountService;
    this.ledgerEntryRepository = ledgerEntryRepository;
  }

  @GetMapping("/{accountNumber}")
  public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
    Account account = accountService.getAccountByNumber(accountNumber);
    return ResponseEntity.ok(
        new AccountResponse(
            account.getAccountNumber(),
            account.getBalance(),
            account.getCurrency(),
            account.getAccountType(),
            account.getCreatedAt()));
  }

  @GetMapping("/{accountNumber}/entries")
  public ResponseEntity<PaginatedEntriesResponse> getAccountEntries(
      @PathVariable String accountNumber,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    accountService.getAccountByNumber(accountNumber);
    var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    var entryPage = ledgerEntryRepository.findEntriesByAccountNumber(accountNumber, pageable);
    var entries =
        entryPage.getContent().stream()
            .map(
                e ->
                    new EntryWithTransactionResponse(
                        e.getTransaction().getTransactionId(),
                        e.getEntryType(),
                        e.getAmount(),
                        e.getBalanceAfter(),
                        e.getCreatedAt()))
            .toList();
    var pagination =
        new PaginationResponse(
            entryPage.getNumber(),
            entryPage.getSize(),
            entryPage.getTotalElements(),
            entryPage.getTotalPages());
    return ResponseEntity.ok(new PaginatedEntriesResponse(entries, pagination));
  }
}