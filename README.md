# Payment Processor

A RESTful payment processing service built with Spring Boot. It handles account management, payment creation with idempotency, and refunds using a double-entry ledger model.

## Tech Stack

- **Java 21** / **Spring Boot 4.0.3**
- **PostgreSQL 17** with **Flyway** migrations
- **Spring Data JPA** (Hibernate)
- **Spring Security** — API key authentication
- **SpringDoc OpenAPI** — auto-generated API docs
- **Testcontainers** — integration tests against a real Postgres instance
- **Docker Compose** — local dev environment (Postgres + Adminer)
- **k6** — load testing (`loadtest.js`)

## How It Works

- **Accounts** hold a balance in a given currency.
- **Payments** transfer funds between two accounts. Each payment is recorded as a pair of debit/credit **ledger entries**, keeping a full audit trail.
- **Idempotency** is enforced via a client-supplied request ID, so duplicate submissions are safe.
- **Refunds** reverse a completed payment by creating opposite ledger entries.
- **Accounts** are currently seeded via a Flyway migration with preset balances for demo purposes. There is no account creation or deposit API yet.

## Future Work

- Fraud detection and transaction risk scoring
- Account creation and external funding (deposits/withdrawals)
- Multi-currency exchange support

## Running Locally

```bash
# Start Postgres
docker compose up -d

# Run the app
./mvnw spring-boot:run

# Run tests
./mvnw test
```

API docs available at `/swagger-ui.html` when the app is running.

## Load Testing

A k6 load test script is included:

```bash
k6 run loadtest.js
```