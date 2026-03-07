package io.github.tare99.paymentprocessor.exception;

/** Thrown when a requested payment transaction does not exist. */
public class PaymentNotFoundException extends RuntimeException {

  public PaymentNotFoundException(String message) {
    super(message);
  }
}
