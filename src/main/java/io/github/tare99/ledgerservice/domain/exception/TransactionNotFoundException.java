package io.github.tare99.ledgerservice.domain.exception;

public class TransactionNotFoundException extends RuntimeException {

  public TransactionNotFoundException(String message) {
    super(message);
  }
}