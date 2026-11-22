# T Style Backend

**REST API and backend services for the T Style fashion marketplace and entertainment platform.**

## 1. Overview

The T Style backend is a Java-based REST API responsible for user management, authentication, authorization, data persistence, and business logic.

It is developed using Java 21, Spring Boot, Spring Data JPA, Spring Security, and MySQL. The application is packaged as a WAR file for deployment on Apache Tomcat.

## 2. Technologies

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 4.1.2 | Application framework |
| Spring Web MVC | REST API |
| Spring Data JPA | Data persistence |
| Hibernate | ORM |
| Spring Security | Authentication and authorization |
| JWT (JJWT 0.13.0) | Token-based authentication |
| BCrypt | Password hashing |
| Spring Validation | Request validation |
| Spring Mail | Email delivery |
| MySQL | Relational database |
| Maven | Dependency and build management |
| JUnit 5 and Mockito | Automated testing |
| Apache Tomcat | WAR deployment |

## 3. Project Structure

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/co/edu/unbosque/tstyle/
│   │   │   ├── config/
│   │   │   ├── email/
│   │   │   ├── exception/
│   │   │   ├── security/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── filter/
│   │   │   │   └── service/
│   │   │   └── user/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── enums/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   └── resources/
│   └── test/
│       └── java/co/edu/unbosque/tstyle/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## 4. Implemented Features

### User Management

- User registration through REST endpoints.
- Email normalization and duplicate validation.
- Password hashing using BCrypt.
- Automatic assignment of the CLIENT role.
- Email verification using six-digit verification codes.
- Verification code expiration and attempt limits.
- Resending verification codes with cooldown control.

### Authentication and Security

- User login with JWT authentication.
- Stateless authentication using Spring Security.
- JWT signature and expiration validation.
- Authenticated user profile retrieval.
- Role-based authorization (`CLIENT` and `ADMIN`).
- Protected endpoints.
- Token invalidation after password changes.

### Password Management

- Password recovery through email verification codes.
- Temporary recovery codes with expiration.
- Attempt limits and resend cooldown.
- Password updates using BCrypt.

### Error Handling

Centralized exception handling for:

- Business rule violations.
- Duplicate resources.
- Missing resources.
- Request validation errors.
- Unexpected server errors.
- Unauthorized and forbidden requests.

## 5. API Endpoints

### User Management

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/api/users/register` | Register a new user | Public |
| POST | `/api/users/verify-email` | Verify user email | Public |
| POST | `/api/users/resend-verification` | Resend verification code | Public |

### Authentication

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/api/auth/login` | Authenticate a user | Public |
| GET | `/api/auth/me` | Retrieve authenticated user profile | Authenticated |
| POST | `/api/auth/forgot-password` | Request password recovery | Public |
| POST | `/api/auth/reset-password` | Reset user password | Public |

All endpoints use JSON request and response bodies.

## 6. Security Model

The application uses stateless JWT authentication.

After successful login, the API returns a signed JWT containing the user's identity and role.

Protected requests must include the following HTTP header:

```http
Authorization: Bearer YOUR_JWT_TOKEN
```

Public registration assigns the `CLIENT` role automatically.

Administrative routes under `/api/admin/**` require the `ADMIN` role.

Passwords and temporary verification codes are stored as BCrypt hashes rather than plaintext values.

## 7. Database

The backend uses MySQL with Spring Data JPA and Hibernate.

Database entities are mapped through JPA annotations.

The current Hibernate configuration uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

This allows Hibernate to update the database schema based on entity changes.

**Important:** Database credentials must remain private and must never be committed to the repository.

## 8. Configuration

Create a local `application.properties` file inside:

```text
src/main/resources/application.properties
```

Configure the database connection, JWT secret, and email service using the values required by your development environment.

Do not publish real credentials, JWT secrets, email passwords, or private configuration files.

The JWT signing secret must be sufficiently strong and must be configured independently for each environment.

## 9. Requirements

- JDK 21.
- Maven or the included Maven Wrapper.
- MySQL database access.
- Git.
- Appropriate database and email configuration.

## 10. Build and Testing

Run the automated tests:

**Windows:**

```bash
mvnw.cmd clean test
```

**Linux / macOS:**

```bash
./mvnw clean test
```

Generate and verify the deployable WAR:

**Windows:**

```bash
mvnw.cmd clean verify
```

**Linux / macOS:**

```bash
./mvnw clean verify
```

The generated WAR file is located inside:

```text
target/
```

## 11. Deployment

The backend is packaged as a WAR application for deployment on Apache Tomcat.

Before deployment, verify:

- Java compatibility with the target environment.
- Database connectivity.
- JWT secret configuration.
- Email service configuration.
- Application startup and endpoint availability.

The application must not depend on modifications to the professor's Tomcat installation.

## 12. Testing Status

The latest recorded complete test execution reported:

- 98 tests executed.
- 0 failures.
- 0 errors.
- Successful Maven test execution.

The final verification must be performed using JDK 21.

## 13. Development Workflow

The project uses Git and GitHub for collaborative development.

- `main`: Main project branch.
- `back`: Backend integration branch.
- `front`: Frontend integration branch.
- `feature/*`: Feature development branches.

Changes must be submitted through Pull Requests and reviewed before merging.

## 14. Current Development Status

### Implemented

- Backend project configuration.
- User registration.
- Email verification.
- BCrypt password hashing.
- JWT authentication.
- Role-based authorization.
- Password recovery and reset.
- Automated authentication and security tests.

### Pending or Under Development

- Initial administrator account provisioning.
- Authentication and frontend integration.
- Administrative functionality and audit event management.
- Additional business modules and external API integrations.
- Final deployment and environment configuration validation.

---

**T Style Backend | Java 21 · Spring Boot · MySQL**

---

## 15. Catalog and External Integrations (Module B)

Read-only product catalog and movie search with trailers, added on top of the existing authentication module.

### Endpoints

| Route | Access | Description |
|---|---|---|
| `GET /api/products` | Public | Filters `categoryId`, `providerId`, `search` (max 100), `page`, `size` (1-50), `sortBy` (`name`, `price`, `id`), `direction` |
| `GET /api/products/{id}` | Public | Product detail; 404 if it does not exist or its provider is inactive |
| `GET /api/movies?query=&page=` | Authenticated | TMDB search (2-100 characters, pages 0-19, 10-minute cache) |
| `GET /api/movies/{id}` | Authenticated | Movie detail with `trailerUrl` (YouTube link reported by TMDB, 1-hour cache) |

The catalog exposes no create, update or delete operations. `/api/admin/**` remains restricted to `ADMIN`. The only change to `SecurityConfig` is a public rule for `GET /api/products` and `GET /api/products/**`.

### `ProductResponse` contract (used by wishlist and cart)

`id`, `provider`, `externalId`, `name`, `imageUrl`, `price`, `currency`, `externalUrl`, `providerAvailable`.

- `price` and `currency` are the provider's original values; no currency conversion is performed.
- `providerAvailable` is the availability reported by the provider and may be `null`. It is not internal stock.
- A product is identified by provider + `externalId`, never by title.

### TMDB configuration

- Base URL: `https://api.themoviedb.org/3` (`tmdb.base-url`), authenticated with the `Bearer` header.
- The read access token is provided only through the `TMDB_ACCESS_TOKEN` environment variable. Without it, `/api/movies` answers `503`.

### Catalog data import

Product records come from a JSON list (not the professor's schema matrix). Set `catalog.import.enabled=true` and `catalog.import.path` (`file:` or `classpath:`) to import once at startup. Invalid records are skipped and logged, a missing or malformed file does not stop the application, and re-importing updates by provider + `externalId` instead of duplicating. `scripts/generar_catalogo.py` builds a demonstration file from DummyJSON.

**Current limitation:** there is no real clothing-provider integration yet. The `ProviderAdapter` interface is ready for one; until then the catalog is filled with demonstration data.

### Pending

- Authorized provider adapter (credentials and permission required).
- HTTP import endpoint restricted to `ADMIN`.
