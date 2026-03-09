package io.github.tare99.paymentprocessor.service;

import io.github.tare99.paymentprocessor.entity.Account;
import io.github.tare99.paymentprocessor.service.dto.SenderAndReceiver;

public interface AccountService {
  Account getAccountByNumber(String accountNumber);

  SenderAndReceiver getSenderAndReceiverForUpdate(
      String senderAccountNumber, String receiverAccountNumber);
}
