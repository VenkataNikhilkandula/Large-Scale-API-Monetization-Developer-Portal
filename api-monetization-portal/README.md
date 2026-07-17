# Enterprise API Monetization & Developer Portal

This repository contains the complete implementation of a production-ready, scalable **API Monetization & Developer Portal** built using Java 21, Spring Boot 3.3.x, MySQL, Redis, Apache Kafka, and Spring Security with RBAC.

## Architectural Design

The system implements a layered architecture adhering to **SOLID** principles:
- **Presentation Layer (Controllers)**: Handles REST API request validation, response shaping, and OpenAPI tagging.
- **Security & Gateway (Rate Limiter Filter)**: Validates security sessions via JWT. Simulates API Gateway behavior for client applications using API Keys. Checks real-time request counts in Redis and aggregates monthly quotas.
- **Business Layer (Services)**: Implements registration, lifecycle state changes, billing logic, and event orchestration.
- **Asynchronous Ingestion (Kafka)**: Non-blocking ingestion logging of API hits to ensure under 300 ms response times for client gateway execution.
- **Data Layer (Spring Data JPA / MySQL)**: Normalised primary tables representing resources, states, and relations.

---

## Technical Stack & Ports

- **Java**: Version 21
- **Spring Boot**: 3.3.1
- **Security**: JWT & Role-Based Access Control (`ROLE_DEVELOPER`, `ROLE_ADMIN`)
- **Primary Database**: MySQL 8.x (Port `3306`)
- **Key-Value Cache**: Redis 7.x (Port `6379`)
- **Message Broker**: Apache Kafka + Zookeeper (Port `9092`)
- **Swagger Documentation**: Springdoc OpenAPI (Port `8080`)

---

## Setup & Running Guide

### 1. Prerequisite Infrastructure (Docker)
Start MySQL, Redis, Zookeeper, and Kafka containers using Docker Compose from the project root:

```bash
docker-compose up -d
```

Verify that all containers are healthy:
```bash
docker ps
```

### 2. Build and Run the Spring Boot App
Compile and package the application using Maven:
```bash
mvn clean package
```

Run the application:
```bash
mvn spring-boot:run
```

Once running, the application will automatically perform database migrations via **Flyway** and seed mock resources.

---

## Database Schema (ER Logical Relations)

```
  +---------------+        +--------------+        +------------------+
  |     users     | 1 ---* |  user_roles  | * --- 1|      roles       |
  +---------------+        +--------------+        +------------------+
          | 1                                               
          |                                                 
          *                                                 
  +---------------+        +--------------+        +------------------+
  | consumer_apps | 1 ---* | api_keys     |        |      apis        |
  +---------------+        +--------------+        +------------------+
          | 1                      1                        | 1
          |                        |                        |
          *                        *                        *
  +---------------+        +--------------+        +------------------+
  |   invoices    |        | subscriptions| * --- 1|    api_usages    |
  +---------------+        +--------------+        +------------------+
```

---

## Verification & Interactive Testing

### 1. API Documentation
Open your web browser and navigate to:
[Swagger UI documentation (http://localhost:8080/swagger-ui.html)](http://localhost:8080/swagger-ui.html)

Here you will find full schemas and interactive forms to test all endpoints.

### 2. Client Authentication & Flow Demonstration

#### Step A: User Login
Generate a JWT bearer token for the seeded developer account:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "developer", "password": "password123"}'
```

#### Step B: Call Gateway Mock Route (API key validation and rate limiting)
Use the pre-seeded key `key_fintech_premium_123` to query the gateway mock sandbox representing the Payment API (ID: `2`):

```bash
curl -X GET http://localhost:8080/gw/api/2/checkouts \
  -H "X-API-KEY: key_fintech_premium_123"
```

Response format:
```json
{
  "status": "SUCCESS",
  "message": "API Gateway successfully routed the request!",
  "apiId": 2,
  "requestPath": "/gw/api/2/checkouts",
  "httpMethod": "GET",
  "timestamp": 17823901923,
  "gatewayNode": "monetization-gateway-node-1"
}
```

*Note: Rapidly repeating this request above the preconfigured threshold (e.g. 50 times per second for PREMIUM) will trigger a fast Redis rejection response:*
```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Too Many Requests: Rate limit exceeded for plan PREMIUM"
}
```

### 3. Running Unit Tests
Run the project's test suite via Maven:
```bash
mvn test
```
