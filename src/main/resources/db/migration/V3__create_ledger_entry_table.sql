CREATE TABLE ledger_entry
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT         NOT NULL REFERENCES ledger_transaction (id),
    account_id     BIGINT         NOT NULL REFERENCES account (id),
    entry_type     VARCHAR(10)    NOT NULL,
    amount         DECIMAL(15, 2) NOT NULL,
    balance_after  DECIMAL(15, 2) NOT NULL,
    created_at     TIMESTAMP      NOT NULL
);

CREATE INDEX idx_ledger_entry_transaction_id ON ledger_entry (transaction_id);
CREATE INDEX idx_ledger_entry_account_id ON ledger_entry (account_id);
CREATE INDEX idx_ledger_entry_account_created ON ledger_entry (account_id, created_at);
