package io.github.tare99.paymentprocessor.service.dto;

import io.github.tare99.paymentprocessor.entity.Account;
import java.math.BigDecimal;

public record SenderAndReceiver(Account sender, Account receiver) {
  public void applyPayment(BigDecimal amount) {
    sender.debit(amount);
    receiver.credit(amount);
  }

  public void applyRefund(BigDecimal amount) {
    sender.credit(amount);
    receiver.debit(amount);
  }
}
