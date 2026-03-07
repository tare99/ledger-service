package io.github.tare99.paymentprocessor.exception;

/** Thrown when an account lookup fails to find the requested account. */
public class AccountNotFoundException extends RuntimeException {

  public AccountNotFoundException(String message) {
    super(message);
  }
}
