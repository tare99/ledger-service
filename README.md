# Ledger Service

An internal double-entry ledger service built with Spring Boot. It records financial transactions as balanced debit/credit entries across accounts, with full audit trail, idempotency, and pessimistic locking for concurrency safety.

## Tech Stack

- **Java 21** / **Spring Boot 4.0.3**
- **PostgreSQL 17** with **Flyway** migrations
- **Spring Data JPA**
- **SpringDoc OpenAPI** — Swagger UI at `/swagger-ui.html`
- **Testcontainers** — integration tests against a real Postgres instance
- **Docker Compose** — Postgres + Adminer for local dev

## How It Works

- **Accounts** hold a balance in a given currency (USD, EUR, GBP, etc.) and have a type (ASSET, LIABILITY, REVENUE, EXPENSE, EQUITY) that determines their normal balance direction.
- **Transactions** record a set of balanced ledger entries — total debits must equal total credits. Each transaction gets a unique ULID.
- **Ledger entries** are immutable and store a balance-after snapshot for auditability.
- **Idempotency** is enforced via a client-supplied `idempotencyKey`, so duplicate submissions return the original transaction.
- **Reversals** undo a posted transaction by creating opposite entries and restoring account balances.
- **Currency validation** ensures all accounts in a transaction share the same currency.
- **Pessimistic write locks** on accounts prevent race conditions during concurrent transactions.

## API

All endpoints are under `/api/v1`.

| Method | Endpoint                            | Description                          |
|--------|-------------------------------------|--------------------------------------|
| GET    | `/accounts/{accountNumber}`         | Get account details                  |
| GET    | `/accounts/{accountNumber}/entries` | Paginated ledger entries for account |
| POST   | `/transactions`                     | Create a new transaction             |
| GET    | `/transactions/{id}`                | Get transaction details              |
| POST   | `/transactions/{id}/reverse`        | Reverse a posted transaction         |

## Running Locally

```bash
# Start Postgres + Adminer
docker compose up -d

# Run the app
./mvnw spring-boot:run

# Run tests
./mvnw test
```

- App: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Adminer: `http://localhost:5050`
