Enterprise API Monetization & Developer Portal

A production-ready API Monetization & Developer Portal built with Java 21, Spring Boot 3.3.x, MySQL, Redis, Apache Kafka, JWT Authentication, and Role-Based Access Control (RBAC).

This platform enables API providers to publish APIs, manage subscriptions, generate API keys, track API usage in real time, and automate billing through a scalable event-driven architecture.

Features
> Authentication & Authorization
> JWT Authentication
> Spring Security
> Role-Based Access Control (RBAC)
> BCrypt Password Encryption
> Developer & Admin Roles

API Management
> Publish APIs
> Draft & Published API States
> API Versioning
> API Catalog
> API Details

Consumer Applications
> Register Consumer Applications
> Manage Applications
> Generate API Keys
> Activate/Deactivate API Keys
> 
Subscription Management
> Subscribe to APIs
> Basic & Premium Subscription Plans
> Active Subscription Tracking

API Gateway Simulation
> API Key Validation
> Request Authentication
> Rate Limiting
> Redis Caching
> Usage Tracking
> Usage Analytics

API Usage Logging
> Kafka Event Streaming
> Usage Dashboard
> API Metrics
> Developer Analytics

Billing
> Monthly Invoice Generation
> Paid & Pending Status
> Invoice History

Documentation
> Swagger UI
> OpenAPI 3 Documentation
> Postman Collection Included

Tech Stack

Technology	Version
Java	21
Spring Boot	3.3.x
Spring Security	Latest
Spring Data JPA	Latest
MySQL	8.x
Redis	7.x
Apache Kafka	Latest
Flyway	Latest
JWT	Latest
Maven	3.9+
Docker	Latest
Swagger OpenAPI	Latest

Project Structure

api-monetization-portal
│
├── controller
├── service
├── repository
├── entity
├── dto
├── config
├── security
├── kafka
├── util
├── exception
│
├── resources
│   ├── db/migration
│   ├── application.properties
│   └── static
│
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
Database Schema

The application uses the monetization_portal database.

Main tables:

users
roles
user_roles

consumer_apps

apis

subscriptions

api_keys

api_usages

invoices

audit_logs

flyway_schema_history
Prerequisites
Java 21
Maven 3.9+
Docker Desktop
MySQL 8
Redis
Apache Kafka
Git
Clone Repository
git clone https://github.com/<your-username>/api-monetization-portal.git

cd api-monetization-portal
Run Infrastructure

Start MySQL, Redis, Kafka and Zookeeper

docker compose up -d

Verify

docker ps
Build Project
mvn clean install
Run Application
mvn spring-boot:run

or

java -jar target/api-monetization-portal.jar

Application starts on

http://localhost:8080
Swagger UI
http://localhost:8080/swagger-ui/index.html
OpenAPI Documentation
http://localhost:8080/v3/api-docs
Authentication
Register
POST /api/auth/register
Login
POST /api/auth/login

Returns

JWT Access Token
User Details

Use the JWT token for authenticated endpoints.

Authorization

Bearer <token>
Main REST APIs
Authentication
Register
Login
APIs
Create API
Get APIs
Get API Details
Consumer Apps
Register App
View Apps
Subscriptions
Subscribe API
View Subscriptions
API Keys
Generate API Key
View API Keys
Usage
API Usage Tracking
Usage Dashboard
Billing
Generate Invoice
Invoice History
Analytics
API Analytics
Developer Analytics
Event-Driven Flow
API Request

      │

Validate API Key

      │

Redis Rate Limiter

      │

Business Logic

      │

Kafka Producer

      │

Kafka Consumer

      │

Store Usage

      │

Generate Analytics
Security Features
JWT Authentication
BCrypt Password Hashing
Role-Based Authorization
API Key Authentication
Redis Rate Limiting
Global Exception Handling
Input Validation
Flyway Database Migration

Database migrations execute automatically during application startup.

Migration scripts include:

V1__schema.sql

V2__seed_data.sql

V3__fix_passwords.sql

V4__fix_bcrypt_passwords.sql
Docker Services
Service	Port
Spring Boot	8080
MySQL	3306
Redis	6379
Kafka	9092
Zookeeper	2181
Sample Users
Username	Role
admin	ROLE_ADMIN
developer	ROLE_DEVELOPER
API Testing

A Postman collection is included in the repository:

api-monetization-portal.postman_collection.json

Import it into Postman to test all available endpoints.

Future Enhancements
API Marketplace
Payment Gateway Integration
Stripe Billing
Email Notifications
OAuth2 Login
Multi-Tenant Support
API Plans & Pricing
Prometheus Monitoring
Grafana Dashboards
Kubernetes Deployment
