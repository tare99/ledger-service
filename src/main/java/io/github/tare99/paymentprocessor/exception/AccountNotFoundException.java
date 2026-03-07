package io.github.tare99.paymentprocessor.exception;

public class AccountNotFoundException extends RuntimeException {

  public AccountNotFoundException(String message) {
    super(message);
  }
}
