# Validation Record

Last updated: **4 October 2026**

This document records what has actually been validated for the current local baseline. It deliberately separates verified behavior from roadmap items.

## Java test suite

Environment used for the original baseline validation:

- Linux
- OpenJDK 17
- Maven 3.9.12

The Maven verification produced four Spring Boot JARs and completed successfully.

| Test suite | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| `it.castrese.lab.api.GatewayTest` | 3 | 0 | 0 | 0 |
| `it.castrese.lab.auth.AuthApiTest` | 5 | 0 | 0 | 0 |
| `it.castrese.lab.customer.CustomerApiTest` | 5 | 0 | 0 | 0 |
| `it.castrese.lab.order.CustomerClientTest` | 4 | 0 | 0 | 0 |
| `it.castrese.lab.order.OrderApiTest` | 4 | 0 | 0 | 0 |

**Total: 21 tests passed, 0 failures, 0 errors, 0 skipped.**

The suites cover:

- API Gateway routing and Authorization header preservation
- Login and password verification
- Real RSA signing behavior in the authentication module
- JWT issuer, audience and expiration validation
- Customer validation, email normalization and persistence behavior
- Order persistence after customer validation
- Failure handling when customer validation fails
- REST client behavior for 404, 500 and connection failures
- Flyway migrations and Hibernate schema validation in the test environment

## Docker Compose baseline

The complete local stack has been started successfully with:

- PostgreSQL 16
- API Gateway
- Auth Service
- Customer Service
- Order Service

The five containers reached a healthy state in the recorded local run.

Only the gateway is published to the host by default:

```text
http://localhost:8088
```

The application services and PostgreSQL communicate on the internal Compose network.

## End-to-end smoke test

The real HTTP smoke test has been executed against the running Docker stack.

The verified flow includes:

1. gateway readiness
2. unauthorized customer request
3. unauthorized order request
4. rejected login with invalid credentials
5. successful login
6. invalid customer payload rejection
7. customer creation
8. duplicate-email conflict
9. customer retrieval
10. rejection of an order for a missing customer
11. invalid order payload rejection
12. order creation
13. order retrieval
14. paginated order listing

The smoke test uses the generated local credentials and a real JWT through the running stack. It does not print credentials or tokens.

Run it with:

```bash
BASE_URL=http://127.0.0.1:8088 python3 scripts/smoke-test.py
```

## CI/CD validation

The repository includes a GitHub Actions workflow that:

1. executes `mvn verify`
2. starts the complete Docker Compose stack
3. runs the HTTP smoke test
4. builds one image per service
5. publishes images to GitHub Container Registry on eligible pushes/tags

The local operations record documents a successful GitHub Actions execution on `main`, and the GHCR-based Compose path has also been exercised locally.

## Security checks

Verified repository safeguards include:

- `.env` excluded from Git
- `.secrets/` excluded from Git
- generated RSA private key excluded from Git
- generated database/bootstrap credentials excluded from Git
- repository contains only placeholder values in `.env.example`
- JWT private key mounted only into `auth-service` in the Compose configuration
- downstream services validate signed JWTs independently
- application containers run as non-root users
- Linux capabilities are dropped in the Compose configuration

## Scope limitations

The following items are **not claimed as completed production capabilities**:

- Kubernetes deployment is not yet part of the verified `main` baseline
- Azure Container Registry deployment is not yet verified
- AKS deployment is not yet verified
- Azure Monitor / Log Analytics / Application Insights are not yet integrated
- TLS, public DNS and production ingress are not yet configured
- centralized cloud secret management is not yet integrated
- production backup/restore procedures are not yet verified
- distributed tracing and production alerting are not yet implemented

These items remain roadmap work and will be moved into the verified section only after implementation and real validation.

## Test-environment caveat

Selected persistence tests use H2 in PostgreSQL compatibility mode. H2 is useful for fast automated tests but does not replace PostgreSQL validation.

That gap is covered by the Docker Compose smoke test, which exercises the application against a real PostgreSQL 16 instance.
