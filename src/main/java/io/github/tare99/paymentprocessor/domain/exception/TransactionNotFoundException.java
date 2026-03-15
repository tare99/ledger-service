package io.github.tare99.paymentprocessor.domain.exception;

public class TransactionNotFoundException extends RuntimeException {

  public TransactionNotFoundException(String message) {
    super(message);
  }
}