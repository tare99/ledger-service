package io.github.tare99.paymentprocessor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.tare99.paymentprocessor.entity.Account;
import io.github.tare99.paymentprocessor.exception.AccountNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AccountServiceIT extends BaseIT {

  @Autowired private AccountService accountService;

  @Test
  void getAccountByNumberReturnsAccount() {
    Account account = accountService.getAccountByNumber("ACC-ALICE00000000001");

    assertThat(account).isNotNull();
    assertThat(account.getAccountNumber()).isEqualTo("ACC-ALICE00000000001");
  }

  @Test
  void getAccountByNumberThrowsWhenNotFound() {
    assertThatThrownBy(() -> accountService.getAccountByNumber("ACC-NONEXISTENT"))
        .isInstanceOf(AccountNotFoundException.class);
  }
}
