package io.github.tare99.paymentprocessor.domain.service;

import io.github.tare99.paymentprocessor.api.request.CreateTransactionRequest;
import io.github.tare99.paymentprocessor.api.response.TransactionResponse;

public interface TransactionService {

  TransactionResponse createTransaction(CreateTransactionRequest request);

  TransactionResponse getTransaction(String transactionId);

  TransactionResponse reverseTransaction(String transactionId);
}