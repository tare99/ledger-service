package io.github.tare99.ledgerservice.domain.service.impl;

import io.github.tare99.ledgerservice.domain.entity.Account;
import io.github.tare99.ledgerservice.domain.exception.AccountNotFoundException;
import io.github.tare99.ledgerservice.domain.repository.AccountRepository;
import io.github.tare99.ledgerservice.domain.service.AccountService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepository;

  public AccountServiceImpl(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Account getAccountByNumber(String accountNumber) {
    return accountRepository
        .findByAccountNumber(accountNumber)
        .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
  }

  @Override
  public List<Account> getAccountsForUpdate(Set<String> accountNumbers) {
    List<Account> accounts = accountRepository.findByAccountNumbersForUpdate(accountNumbers);
    if (accounts.size() != accountNumbers.size()) {
      Set<String> found =
          accounts.stream().map(Account::getAccountNumber).collect(Collectors.toSet());
      Set<String> missing = new HashSet<>(accountNumbers);
      missing.removeAll(found);
      throw new AccountNotFoundException("Accounts not found: " + missing);
    }
    return accounts;
  }
}
