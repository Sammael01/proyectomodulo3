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


---

# T-Style — Frontend (Angular)

Frontend de la tienda T-Style. Se conecta al backend unificado (Spring Boot en Apache Tomcat) y se despliega en Netlify.

## Requisitos

| Herramienta | Versión |
|---|---|
| Node.js | `^22.22.3` o `^24.15.0` (exigido por Angular CLI 22; ver `engines` en `package.json`) |
| npm | 10.x (incluido con Node 22) |
| Angular CLI | 22.2.x (se instala como dependencia del proyecto; no requiere instalación global) |

## Instalación

```bash
cd t-style
npm install
```

## Ejecución local

El backend debe estar corriendo en `http://localhost:8080` (contexto `/api`).

| Comando | Ambiente | URL del backend | Notas |
|---|---|---|---|
| `npm start` | `environment.development.ts` | `http://localhost:8080/api` | Petición directa. El backend debe permitir CORS para `http://localhost:4200`. |
| `npm run start:proxy` | `environment.proxy.ts` | `/api` → proxy → `localhost:8080` | Usa `proxy.conf.json`. No requiere CORS. **Solo desarrollo.** |

Abrir `http://localhost:4200/`.

## Compilación de producción

```bash
npm run build        # configuración production (por defecto)
npm run build:dev    # configuración development, sin optimizar
```

Salida: `dist/t-style/browser/` (esa es la carpeta que se publica; nunca `src/` ni la raíz del proyecto).

## Pruebas

```bash
npm test             # Vitest, una sola ejecución
```

## Ambientes y URL del backend

Los servicios **siempre** importan `src/environments/environment.ts`. Angular lo reemplaza en compilación mediante `fileReplacements` (`angular.json`):

| Archivo | Se usa en | `apiUrl` |
|---|---|---|
| `environment.ts` | `ng build` (producción / Netlify) | URL absoluta del backend en Tomcat — **PENDIENTE** |
| `environment.development.ts` | `ng serve`, `npm run build:dev` | `http://localhost:8080/api` |
| `environment.proxy.ts` | `npm run start:proxy` | `/api` |

**Antes de desplegar en producción**, reemplazar en `src/environments/environment.ts` el valor `https://backend-pendiente.invalid/api` por la URL pública real del backend (formato `https://<host>/<contexto>/api`), confirmada por el integrante A. El dominio `.invalid` nunca resuelve, de modo que un despliegue sin configurar falla de forma visible en lugar de llamar a un servidor equivocado.

Las rutas de cada endpoint están centralizadas en `src/app/core/api-endpoints.ts`. Una ruta en `null` significa que está pendiente de confirmar: el servicio sigue usando datos locales y no envía peticiones. Al confirmar una ruta con A, B o C se escribe allí y el servicio pasa automáticamente a consumir el backend.

## Autenticación

- El JWT se guarda en `localStorage` (`tstyle_session`) y el interceptor (`core/auth.interceptor.ts`) agrega `Authorization: Bearer <token>` **solo** a peticiones cuyo origen y ruta coinciden con `apiUrl` (relativa o absoluta). YouTube, CDNs y otros servicios nunca reciben el token.
- Un `401` de una petición autenticada cierra la sesión y redirige a `/login`.
- Los guards de Angular solo mejoran la navegación. La autorización real la hace Spring Security; el guard de `/admin` además confirma el rol con `/auth/me` antes de abrir el panel.

## Despliegue en Netlify

1. Configurar la URL real del backend en `src/environments/environment.ts` (ver arriba).
2. En Netlify, crear el sitio desde el repositorio y configurar:
   - **Base directory:** la carpeta que contiene este README (`t-style`).
   - **Build command:** `npm run build`
   - **Publish directory:** `dist/t-style/browser`
   - Node 22 (ya definido en `netlify.toml` mediante `NODE_VERSION`).
   `netlify.toml` contiene estos valores, por lo que Netlify los toma automáticamente.
3. `public/_redirects` (`/*  /index.html  200`) se copia al build y permite recargar rutas internas (`/coleccion`, `/producto/123`, `/admin`, …) sin error 404.
4. Entregar al integrante A el dominio exacto de Netlify (p. ej. `https://<sitio>.netlify.app`) para configurar CORS.

## CORS

CORS se configura en el backend, no en Angular. El backend debe permitir:

- **Origen exacto** de producción (dominio de Netlify) y, para desarrollo, `http://localhost:4200`. No usar `*` en peticiones autenticadas.
- **Encabezados:** `Authorization`, `Content-Type`.
- **Métodos:** los que use la aplicación: `GET`, `POST`, `PUT`, `PATCH`, `DELETE` y `OPTIONS` (preflight). Ajustar cuando B y C confirmen sus endpoints.

## Seguridad

- No incluir credenciales, tokens ni claves privadas en el código. El token de TMDB vive solo en el backend.
- `youtubeApiKey` (opcional) solo puede ser una clave de navegador restringida por dominio (referrer de Netlify) y por API (YouTube Data API v3).
- Mientras no exista una pasarela de pago real (`core/checkout.config.ts`), tarjeta y PSE se muestran como simulación; sus datos no se guardan, no se registran ni se envían al backend.
