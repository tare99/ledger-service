package io.github.tare99.paymentprocessor.domain.service;

import io.github.tare99.paymentprocessor.domain.entity.Account;
import java.util.List;
import java.util.Set;

public interface AccountService {
  Account getAccountByNumber(String accountNumber);

  List<Account> getAccountsForUpdate(Set<String> accountNumbers);
}