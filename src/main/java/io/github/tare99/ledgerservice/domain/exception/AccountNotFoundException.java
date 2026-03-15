package io.github.tare99.ledgerservice.domain.exception;

public class AccountNotFoundException extends RuntimeException {

  public AccountNotFoundException(String message) {
    super(message);
  }
}
