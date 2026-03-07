package io.github.tare99.paymentprocessor.exception;

/** Thrown when a payment request is identified as a duplicate via its idempotency key. */
public class DuplicatePaymentException extends RuntimeException {

  public DuplicatePaymentException(String message) {
    super(message);
  }
}
