package io.github.tare99.paymentprocessor.service;

import io.github.tare99.paymentprocessor.entity.Account;

public interface AccountService {
  Account getActiveAccountByNumber(String accountNumber);
}
