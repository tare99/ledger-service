package io.github.tare99.paymentprocessor.domain.entity;

import lombok.Getter;

@Getter
public enum AccountType {
  ASSET(EntryType.DEBIT),
  EXPENSE(EntryType.DEBIT),
  LIABILITY(EntryType.CREDIT),
  REVENUE(EntryType.CREDIT),
  EQUITY(EntryType.CREDIT);

  private final EntryType normalBalance;

  AccountType(EntryType normalBalance) {
    this.normalBalance = normalBalance;
  }

  public boolean increases(EntryType entryType) {
    return entryType == normalBalance;
  }
}
