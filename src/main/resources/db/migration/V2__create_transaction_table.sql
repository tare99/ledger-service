CREATE TABLE ledger_transaction
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id  VARCHAR(30) NOT NULL UNIQUE,
    status          VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(64) NOT NULL UNIQUE,
    description     VARCHAR(255),
    created_at      TIMESTAMP   NOT NULL,
    updated_at      TIMESTAMP   NOT NULL
);

CREATE INDEX idx_transaction_idempotency_key ON ledger_transaction (idempotency_key);
CREATE INDEX idx_transaction_transaction_id ON ledger_transaction (transaction_id);
CREATE INDEX idx_transaction_created_at ON ledger_transaction (created_at);
