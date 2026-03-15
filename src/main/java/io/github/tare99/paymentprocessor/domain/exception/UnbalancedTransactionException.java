package io.github.tare99.paymentprocessor.domain.exception;

public class UnbalancedTransactionException extends RuntimeException {

  public UnbalancedTransactionException(String message) {
    super(message);
  }
}