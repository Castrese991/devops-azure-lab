# Portfolio Case Study

## Java Spring Boot Microservices Platform

**Role:** Backend / Cloud Engineering  
**Project type:** Personal production-style engineering project  
**Repository:** `Castrese991/devops-azure-lab`

## Overview

This project demonstrates the design and implementation of a secure Java microservices platform with a reproducible local environment and an automated CI/CD pipeline.

The system is composed of an API Gateway, authentication service, customer service, order service, and PostgreSQL. Each application is built and containerized independently.

## Problem addressed

The objective was to build a small but realistic backend platform that goes beyond a simple CRUD demo and includes concerns commonly found in professional environments:

- authentication and authorization
- service-to-service communication
- isolated persistence boundaries
- schema migrations
- containerization
- health and readiness checks
- automated testing
- CI/CD
- operational troubleshooting
- preparation for Kubernetes and Azure

## Solution

The platform uses Spring Cloud Gateway as its entry point and Spring Security with RSA-signed JWTs for stateless authentication.

Each business service validates JWTs independently instead of relying only on gateway-level security.

PostgreSQL hosts separate databases and application roles for the authentication, customer, and order services. Flyway manages schema changes.

The entire stack can be started with Docker Compose and validated with a real HTTP smoke test.

## Key implementation details

- Java 17 and Spring Boot
- Spring Cloud Gateway
- Spring Security and OAuth2 Resource Server
- RS256 JWT signing and validation
- BCrypt password hashing
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Flyway migrations
- Docker multi-stage builds
- Docker Compose health checks
- GitHub Actions
- GitHub Container Registry
- JUnit automated tests
- end-to-end Python smoke test
- request correlation through `X-Request-ID`
- liveness and readiness endpoints

## Quality and validation

The Java test suite contains 21 automated tests with no recorded failures in the baseline validation.

The complete local Compose stack has been exercised with all services healthy, followed by an end-to-end test covering authentication, customer creation, duplicate validation, order creation, service-to-service customer validation, and retrieval operations.

The CI workflow runs Maven verification, starts the complete stack, executes the smoke test, builds the service images, and publishes eligible images to GHCR.

## Security considerations

The project includes several deliberate safeguards:

- no production secrets stored in Git
- local credentials generated automatically
- RSA private key excluded from the repository
- BCrypt password storage
- short-lived signed JWTs
- issuer and audience validation
- per-service database credentials
- private application/database networking in Compose
- non-root containers
- reduced Linux capabilities

## Current roadmap

The next infrastructure milestones are:

- local Kubernetes deployment
- Azure Container Registry
- Azure Kubernetes Service
- Azure Monitor
- Log Analytics
- Application Insights
- TLS and DNS
- managed secrets
- distributed tracing and alerting

These are intentionally presented as roadmap items until they are implemented and verified.

## Suggested Upwork portfolio title

**Java Spring Boot Microservices Platform | Docker, JWT, PostgreSQL & CI/CD**

## Suggested Upwork description

> Designed and developed a production-style Java microservices platform using Spring Boot, Spring Security, REST APIs, JWT, PostgreSQL and Docker. The solution includes an API Gateway, authentication, customer and order services, Flyway database migrations, service-to-service communication, health checks, automated tests and a GitHub Actions CI/CD pipeline with container image publishing to GHCR. Built with a strong focus on security, reproducibility and operational readiness.

## Suggested portfolio skills

- Java
- Spring Boot
- Microservices
- REST API
- Spring Security
- PostgreSQL
- Docker
- CI/CD
- GitHub Actions

## Screenshot checklist

Recommended screenshots for a portfolio presentation:

1. GitHub repository landing page with the architecture diagram visible
2. `docker compose ps` with all containers healthy
3. successful smoke-test terminal output
4. successful GitHub Actions workflow
5. repository service structure or selected clean code view

Do not include local secrets, tokens, private keys, real credentials, or sensitive terminal history in screenshots.
