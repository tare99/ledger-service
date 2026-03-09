package io.github.tare99.paymentprocessor.domain.service.impl;

import io.github.tare99.paymentprocessor.domain.entity.Account;
import io.github.tare99.paymentprocessor.domain.exception.AccountNotFoundException;
import io.github.tare99.paymentprocessor.domain.repository.AccountRepository;
import io.github.tare99.paymentprocessor.domain.service.AccountService;
import io.github.tare99.paymentprocessor.domain.service.dto.SenderAndReceiver;
import java.util.List;
import java.util.Map;
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
  public SenderAndReceiver getSenderAndReceiverForUpdate(
      String senderAccountNumber, String receiverAccountNumber) {
    List<Account> accounts =
        accountRepository.findByAccountNumbersForUpdate(
            Set.of(senderAccountNumber, receiverAccountNumber));
    if (accounts.size() != 2) {
      throw new AccountNotFoundException(
          "Accounts not found. Sender "
              + senderAccountNumber
              + ", receiver "
              + receiverAccountNumber);
    }
    Map<String, Account> accountMap =
        accounts.stream().collect(Collectors.toMap(Account::getAccountNumber, a -> a));
    return new SenderAndReceiver(
        accountMap.get(senderAccountNumber), accountMap.get(receiverAccountNumber));
  }
}
