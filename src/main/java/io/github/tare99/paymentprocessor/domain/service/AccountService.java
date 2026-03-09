package io.github.tare99.paymentprocessor.domain.service;

import io.github.tare99.paymentprocessor.domain.entity.Account;
import io.github.tare99.paymentprocessor.domain.service.dto.SenderAndReceiver;

public interface AccountService {
  Account getAccountByNumber(String accountNumber);

  SenderAndReceiver getSenderAndReceiverForUpdate(
      String senderAccountNumber, String receiverAccountNumber);
}
