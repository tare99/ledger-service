package io.github.tare99.paymentprocessor.exception;

public class PaymentNotFoundException extends RuntimeException {

  public PaymentNotFoundException(String message) {
    super(message);
  }
}
