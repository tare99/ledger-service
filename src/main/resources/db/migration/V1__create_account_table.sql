CREATE TABLE account (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_number VARCHAR(20)     NOT NULL UNIQUE,
    balance     DECIMAL(15, 2)      NOT NULL DEFAULT 0.00,
    currency    VARCHAR(3)          NOT NULL,
    created_at  TIMESTAMP           NOT NULL,
    updated_at  TIMESTAMP           NOT NULL
);

CREATE INDEX idx_account_account_number ON account (account_number);