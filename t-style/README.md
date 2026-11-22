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
