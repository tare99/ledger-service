package io.github.tare99.ledgerservice.domain.exception;

public class UnbalancedTransactionException extends RuntimeException {

  public UnbalancedTransactionException(String message) {
    super(message);
  }
}