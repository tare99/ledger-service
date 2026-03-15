package io.github.tare99.ledgerservice.domain.service;

import io.github.tare99.ledgerservice.api.request.CreateTransactionRequest;
import io.github.tare99.ledgerservice.api.response.TransactionResponse;

public interface TransactionService {

  TransactionResponse createTransaction(CreateTransactionRequest request);

  TransactionResponse getTransaction(String transactionId);

  TransactionResponse reverseTransaction(String transactionId);
}