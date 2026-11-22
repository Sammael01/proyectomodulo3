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

- Angular 22 (standalone components, signals, `@angular/build:application` builder)
- TypeScript 6
- RxJS 7
- Angular Router, Reactive Forms and HttpClient
- Bootstrap 5, Bootstrap Icons and Animate.css (loaded from CDN)
- Vitest (unit testing)
- Netlify (deployment)

### Development Tools

- Eclipse IDE
- Visual Studio Code
- Git and GitHub
- Maven Wrapper
- Node.js and npm
- Angular CLI
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
├── t-style/                      # Angular frontend
│   ├── public/
│   │   └── _redirects            # Netlify SPA rule
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/       # Pages and UI components
│   │   │   └── core/             # Services, interceptor, guards, models, API config
│   │   └── environments/         # Production, development and proxy environments
│   ├── docs/                     # Frontend integration report
│   ├── angular.json
│   ├── netlify.toml
│   ├── package.json
│   └── proxy.conf.json
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

## Frontend Features

### Pages

| Route | Description |
|---|---|
| `/` | Home page with featured products and categories |
| `/coleccion` | Product catalog with search and filters |
| `/producto/:id` | Product detail with size and quantity selection |
| `/carrito` | Shopping cart with coupons and shipping estimate |
| `/checkout` | Checkout (requires login) |
| `/videos` | T-Style TV video section |
| `/contacto` | Contact form |
| `/login`, `/registro`, `/verificar`, `/recuperar` | Login, registration, email verification and password recovery |
| `/perfil` | User profile and orders (requires login) |
| `/admin` | Administration panel for products and orders (requires `ADMIN` role) |

### Authentication

- JWT session stored in the browser and restored on page reload while the token is valid.
- Automatic logout when the token expires or the backend responds `401`.
- HTTP interceptor that adds `Authorization: Bearer <token>` **only** to requests sent to the configured backend URL (relative or absolute). External services such as YouTube never receive the token.
- Route guards for authenticated users and administrators. The admin guard confirms the role with the backend (`/auth/me`) instead of trusting data stored in the browser.
- Final authorization is always enforced by the backend (Spring Security).

### Backend Integration Status

All API routes are centralized in `t-style/src/app/core/api-endpoints.ts`. A route set to `null` is pending confirmation with the backend team; while it is `null`, the related feature keeps working with local demo data and sends no HTTP requests.

| Module | Frontend routes | Status |
|---|---|---|
| Users and security (A) | `/auth/login`, `/users/register`, `/users/verify-email`, `/users/resend-verification`, `/auth/forgot-password`, `/auth/reset-password`, `/auth/me` | Consumed by the frontend; only `/users/register` is implemented in the backend |
| Product catalog (B) | List, detail, search, admin create/update/delete | Pending definition — local demo catalog (12 products) |
| Movies and trailers (B) | Video list (TMDB through the backend) | Pending definition — local demo videos |
| Cart (C) | Get, add, update, remove, clear | Pending definition — cart stored in the browser |
| Orders (C) | Create order, my orders, admin list, update status | Pending definition — orders stored in the browser |

### Checkout and Payments

- Prices, discounts, shipping and totals shown in the frontend are estimates; the backend must calculate and validate the final values.
- Order requests send only product IDs, sizes, quantities, shipping data and payment method — never prices or totals.
- No real payment gateway is integrated. Card and PSE payments are clearly labeled as simulations; card data is never stored, logged or sent to the backend.

## Security

The current implementation includes:

- BCrypt password hashing.
- Request validation.
- User role definitions.
- Automatic assignment of the client role during public registration.
- Frontend JWT handling restricted to backend requests.
- No secrets, tokens or private API keys in the frontend code.

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

### Backend

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

### Frontend

The frontend uses Vitest through the Angular CLI.

The latest frontend verification reported:

- 21 tests executed, 0 failures.
- Successful production build with no TypeScript errors.

Testing covers:

- Backend URL detection (relative and absolute URLs).
- JWT interceptor behavior, including that external services do not receive the token.
- `401` handling, session expiration and session restore.
- Admin guard role verification against the backend.
- Local mode without requests to unconfirmed endpoints.

## Prerequisites

- Java Development Kit (JDK) 21.
- Maven (or the included Maven Wrapper).
- MySQL access and database credentials for backend integration.
- Git.
- Node.js `^22.22.3` or `^24.15.0` and npm (required by Angular CLI 22).

## Installation Instructions

Clone the repository:

```bash
git clone https://github.com/Sammael01/proyectomodulo3.git
cd proyectomodulo3
```

Backend:

```bash
cd backend
```

Frontend:

```bash
cd t-style
npm install
```

## Configuration

### Backend

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

### Frontend

The backend URL is configured per environment in `t-style/src/environments/`:

| Command | Environment file | Backend URL |
|---|---|---|
| `npm start` | `environment.development.ts` | `http://localhost:8080/api` |
| `npm run start:proxy` | `environment.proxy.ts` | `/api` (Angular proxy to `localhost:8080`, development only) |
| `npm run build` | `environment.ts` | Production backend URL — **pending** |

Before deploying, replace the placeholder `https://backend-pendiente.invalid/api` in `environment.ts` with the public URL of the backend deployed on Tomcat.

### CORS

CORS must be configured in the backend. It must allow:

- The exact Netlify production origin and `http://localhost:4200` for development (do not use `*` for authenticated requests).
- The `Authorization` and `Content-Type` headers.
- The HTTP methods used by the application (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`).

## Build and Testing

### Backend

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

### Frontend

From the `t-style` folder:

```bash
npm start              # development server at http://localhost:4200
npm run start:proxy    # development server using the Angular proxy
npm test               # unit tests (Vitest)
npm run build          # production build
```

The production build is generated in:

```text
t-style/dist/t-style/browser/
```

## Deployment

### Backend

The backend is deployed as a WAR file on Apache Tomcat.

### Frontend (Netlify)

1. Set the production backend URL in `t-style/src/environments/environment.ts`.
2. Create the site in Netlify from this repository with **Base directory** `t-style`. The build command (`npm run build`), publish directory (`dist/t-style/browser`) and Node version are defined in `t-style/netlify.toml`.
3. `public/_redirects` (`/* /index.html 200`) allows reloading internal routes such as `/coleccion` or `/admin` without a 404 error.
4. Share the exact Netlify domain with the backend team to configure CORS.

## Development Workflow

The project uses Git and GitHub for collaborative development.

- `main`: main integration branch.
- `back`: backend integration branch.
- `front`: frontend integration branch.
- `feature/*`: individual feature development branches.

Changes are submitted through Pull Requests so team members can review, discuss, and approve implementations before merging.

## Project Status

**Current development focus: Backend user registration module and frontend–backend integration.**

### Implemented

- Backend project structure.
- User and authentication DTOs.
- Global exception handling.
- JPA user entity and repository.
- User registration service.
- BCrypt password encoding.
- User registration REST endpoint.
- Automated unit and HTTP tests.
- Angular frontend with catalog, product detail, cart, checkout, profile, videos and admin pages.
- Frontend environments for development and production.
- JWT interceptor, session handling and route guards.
- Netlify deployment configuration.
- Frontend unit tests.

### In Progress

- Real MySQL persistence integration.
- End-to-end API verification.
- User authentication and authorization.
- Frontend and backend integration (catalog, cart, orders and videos endpoints).
- Marketplace and entertainment functionality.
- Production backend URL and CORS configuration.

## Team

T Style is developed as a collaborative university software engineering project.

Team members and individual responsibilities will be documented as the project architecture and work assignments are finalized.

---

**T Style | Fashion, Technology and Entertainment.**
