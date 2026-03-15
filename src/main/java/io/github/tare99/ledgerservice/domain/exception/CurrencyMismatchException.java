package io.github.tare99.ledgerservice.domain.exception;

public class CurrencyMismatchException extends RuntimeException {

  public CurrencyMismatchException(String message) {
    super(message);
  }
}