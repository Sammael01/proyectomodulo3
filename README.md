# T Style

**An integrated fashion marketplace and entertainment platform.**

## Description

T Style is a web-based platform designed to bring fashion shopping and entertainment together in one place.

The project aims to provide users with access to clothing products from external marketplaces such as Amazon, Temu, Shein, and Mercado Libre, while integrating real-time information and movie discovery features.

The platform is being developed with Angular on the frontend and Java Spring Boot on the backend.

## Team Members

- Daniel Zapata
- Samuel Contreras
- Samuel Parada
- Sebastian Hernandez

## Project Objectives

- Provide a centralized interface for discovering clothing products from external marketplaces.
- Display product information, prices, and relevant details.
- Support product search and personalized shopping experiences.
- Integrate currency information and external APIs.
- Provide a movie discovery experience with information and YouTube trailers.
- Implement user registration, authentication, and role-based access.
- Provide administrative functionality for managing platform information.

## Technologies Used

### Backend

- Java 21
- Spring Boot 4.1.2
- Spring Data JPA
- Spring Web MVC
- Spring Validation
- Spring Security Crypto
- BCrypt password hashing
- Maven
- MySQL
- JUnit 5
- Mockito
- Apache Tomcat (WAR deployment)

### Frontend

- Angular
- TypeScript

### Development Tools

- Eclipse IDE
- Git and GitHub
- Maven Wrapper
- Postman / Insomnia for API testing

## Project Structure

The repository is organized into separate frontend and backend development areas.

```text
proyectomodulo3/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/co/edu/unbosque/tstyle/
│   │   │   │   ├── config/
│   │   │   ├── exception/
│   │   │   ├── security/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   └── service/
│   │   │   └── user/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── enums/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   └── resources/
│   ├── pom.xml
│   └── mvnw
│
├── .gitignore
├── LICENSE
└── README.md
```

## Backend Features

### User Management

- User entity implemented with JPA.
- User roles defined through an enumeration (`CLIENT` and `ADMIN`).
- User repository implemented with Spring Data JPA.
- User registration service.
- Email normalization and duplicate email validation.
- Automatic assignment of the `CLIENT` role during public registration.
- Password hashing using BCrypt.
- REST controller for user registration.

### API Endpoints

| Method | Endpoint | Description | Status |
|---|---|---|---|
| POST | `/api/users/register` | Register a new user | Implemented |

### Error Handling

The backend includes centralized exception handling for:

- Business rule violations.
- Duplicate resources.
- Resources not found.
- Request validation errors.
- Unexpected server errors.

## Security

The current implementation includes:

- BCrypt password hashing.
- Request validation.
- User role definitions.
- Automatic assignment of the client role during public registration.

Authentication, authorization, and protected endpoints are part of the upcoming development stages.

## Database

The backend is designed to use MySQL as its relational database management system.

Spring Data JPA is used to manage persistence operations and map Java entities to database tables.

Hibernate schema generation is currently disabled to keep database changes under controlled management.

```properties
spring.jpa.open-in-view=false
spring.jpa.hibernate.ddl-auto=none
```

**Important:** Database credentials and connection details must remain private and must never be committed to the repository.

## Testing

The backend uses JUnit 5 and Mockito for automated testing.

The latest complete Maven verification reported:

- 49 tests executed.
- 0 failures.
- 0 errors.
- Successful WAR generation.

Testing covers:

- User model behavior.
- DTO validation.
- Service business logic.
- Password encoding.
- Exception handling.
- REST controller responses.

## Prerequisites

- Java Development Kit (JDK) 21.
- Maven (or the included Maven Wrapper).
- MySQL access and database credentials for backend integration.
- Git.
- Node.js and npm for frontend development.

## Installation Instructions

Clone the repository:

```bash
git clone https://github.com/Sammael01/proyectomodulo3.git
cd proyectomodulo3/backend
```

## Configuration

Create or configure your local `application.properties` file inside `src/main/resources/`.

Use the following template:

```properties
spring.application.name=YOUR_APPLICATION_NAME

spring.datasource.url=YOUR_DATABASE_URL
spring.datasource.username=YOUR_DATABASE_USERNAME
spring.datasource.password=YOUR_DATABASE_PASSWORD
spring.datasource.driver-class-name=YOUR_DATABASE_DRIVER

spring.jpa.open-in-view=false
spring.jpa.hibernate.ddl-auto=none
```

Replace the placeholders with the appropriate values for your development environment.

For the current MySQL configuration, the JDBC driver is:

```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

Do not commit real credentials or sensitive configuration files.

## Build and Testing

Using Windows:

```bash
mvnw.cmd clean verify
```

Using Linux or macOS:

```bash
./mvnw clean verify
```

To run the application locally:

```bash
mvnw.cmd spring-boot:run
```

To generate the deployable WAR:

```bash
mvnw.cmd clean package
```

The generated WAR file is located in:

```text
target/tstylebackend-0.0.1-SNAPSHOT.war
```

## Development Workflow

The project uses Git and GitHub for collaborative development.

- `main`: main integration branch.
- `back`: backend integration branch.
- `front`: frontend integration branch.
- `feature/*`: individual feature development branches.

Changes are submitted through Pull Requests so team members can review, discuss, and approve implementations before merging.

## Project Status

**Current development focus: Backend user registration module.**

### Implemented

- Backend project structure.
- User and authentication DTOs.
- Global exception handling.
- JPA user entity and repository.
- User registration service.
- BCrypt password encoding.
- User registration REST endpoint.
- Automated unit and HTTP tests.

### In Progress

- Real MySQL persistence integration.
- End-to-end API verification.
- User authentication and authorization.
- Frontend and backend integration.
- Marketplace and entertainment functionality.

## Team

T Style is developed as a collaborative university software engineering project.

Team members and individual responsibilities will be documented as the project architecture and work assignments are finalized.

---

**T Style | Fashion, Technology and Entertainment.**
