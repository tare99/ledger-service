package io.github.tare99.paymentprocessor.service.impl;

import io.github.tare99.paymentprocessor.entity.Account;
import io.github.tare99.paymentprocessor.entity.AccountStatus;
import io.github.tare99.paymentprocessor.exception.AccountNotFoundException;
import io.github.tare99.paymentprocessor.repository.AccountRepository;
import io.github.tare99.paymentprocessor.service.AccountService;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepository;

  public AccountServiceImpl(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Account getActiveAccountByNumber(String accountNumber) {
    return accountRepository
        .findByAccountNumberAndStatus(accountNumber, AccountStatus.ACTIVE)
        .orElseThrow(
            () -> new AccountNotFoundException("Active account not found: " + accountNumber));
  }
}
