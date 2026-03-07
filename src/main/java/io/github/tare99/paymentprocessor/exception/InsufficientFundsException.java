package io.github.tare99.paymentprocessor.exception;

/** Thrown when an account does not have enough balance to cover a debit. */
public class InsufficientFundsException extends RuntimeException {

  public InsufficientFundsException(String message) {
    super(message);
  }
}
