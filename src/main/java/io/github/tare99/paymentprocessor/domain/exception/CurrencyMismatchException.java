package io.github.tare99.paymentprocessor.domain.exception;

public class CurrencyMismatchException extends RuntimeException {

  public CurrencyMismatchException(String message) {
    super(message);
  }
}