# Informe — Preparación del frontend T-Style para la integración (Integrante D)

Rama: `feature/frontend-preparacion-integracion` (creada sobre el código recibido; `main` no se modificó).

## 1. Archivos modificados

| Archivo | Cambio |
|---|---|
| `angular.json` | `fileReplacements` para `development` y nueva configuración `proxy`; el proxy solo aplica a `serve:proxy`. |
| `package.json` | Scripts `start:proxy`, `build:dev`; `test` en una sola ejecución; `engines.node`. |
| `README.md` | Instalación, ambientes, proxy, build, Netlify, CORS y seguridad. |
| `src/environments/environment.ts` | Ahora es producción: URL absoluta pendiente `https://backend-pendiente.invalid/api`. |
| `src/app/core/auth.interceptor.ts` | Detección de backend por origen + ruta (relativa o absoluta). |
| `src/app/core/auth.service.ts` | Validación de sesión guardada, expiración automática, rutas centralizadas. |
| `src/app/core/guards.ts` | `adminGuard` confirma el rol con `/auth/me`. |
| `src/app/core/api-error.ts` | Mensajes para 400/401/403/404/409/5xx; prioriza el mensaje real del backend. |
| `src/app/core/catalog.service.ts` | `HttpClient`, modo local/API, carga, búsqueda, por id, paginación, CRUD admin. |
| `src/app/core/cart.service.ts` | Sincronización preparada con C, reversión ante error de inventario. |
| `src/app/core/order.service.ts` | `submit()` como Observable, pedidos propios/admin desde backend, estado vía HTTP. |
| `src/app/core/video.service.ts` | Fuente backend (TMDB vía B) preparada; errores visibles. |
| `components/checkoutcomponent/*` | Sin `setTimeout`; envío HTTP, anti doble envío, error real, pago simulado explícito, borrado de datos de tarjeta. |
| `components/admincomponent/*` | Operaciones asíncronas con carga/error, bloqueo de duplicados, aviso de modo demo. |
| `components/catalogcomponent/*`, `productcomponent/*` | Estados de carga y error; consulta por id en modo API. |
| `components/profilecomponent/profilecomponent.ts` | Carga pedidos del usuario (no-op en modo local). |
| `components/videocomponent/videocomponent.html` | Insignia "Demo" cuando el contenido es de demostración. |

## 2. Archivos creados

`src/environments/environment.development.ts`, `src/environments/environment.proxy.ts`, `public/_redirects`, `netlify.toml`, `src/app/core/api-config.ts`, `src/app/core/api-endpoints.ts`, `src/app/core/remote-data.ts`, `src/app/core/checkout.config.ts`, `src/testing/jwt.ts`, pruebas `api-config.spec.ts`, `auth.interceptor.spec.ts`, `auth.service.spec.ts`, `order.service.spec.ts`, y este informe.

## 3. Ambientes

| Comando | Archivo | `apiUrl` |
|---|---|---|
| `npm start` | `environment.development.ts` | `http://localhost:8080/api` |
| `npm run start:proxy` | `environment.proxy.ts` | `/api` (proxy a `localhost:8080`) |
| `npm run build` | `environment.ts` | `https://backend-pendiente.invalid/api` (**reemplazar**) |

Verificado en los bundles: producción contiene solo la URL pendiente; desarrollo contiene `localhost:8080/api`. Los servicios inyectan `API_BASE_URL`, así que cambiar de ambiente no exige tocar servicios.

## 4. Interceptor JWT

- `isBackendUrl()` resuelve la URL de la petición y `apiUrl` contra el origen actual y compara **origen exacto** y **prefijo de ruta por segmento**.
- Rechaza `https://host.evil.com/api`, `https://host/api-otro`, `http://` vs `https://`, y `/api` relativo cuando la base es absoluta (evita enviar el token a Netlify).
- Mantiene el 401: cierra sesión y redirige a `/login?returnUrl=…&expired=1`. Un 401 sin token (credenciales inválidas) no redirige.
- YouTube y la API de tasas de cambio usan `HttpBackend` (sin interceptores) y además no coinciden con `apiUrl`.

## 5. Servicios

Cada servicio tiene `source: 'local' | 'api'`, calculado a partir de `API_ENDPOINTS`. Con endpoints en `null` (estado actual) **no se envía ninguna petición** y se mantiene el comportamiento previo.

- **Auth:** contrato sin cambios (`token, userId, name, email, role`). Sesión inválida o expirada se descarta al recargar; logout automático al expirar el `exp`. `/auth/me` actualiza nombre/correo/rol con los datos del servidor.
- **Catálogo:** `load(query)`, `fetchById`, `search`, `create/update/delete`. Ids del backend se normalizan a string; `newId()` (`p…`) queda solo para modo demo; el catálogo de demo no se persiste en modo API.
- **Carrito:** copia local como apoyo visual; en modo API cada cambio se envía al backend, se sustituye por la respuesta y se revierte con el mensaje real si hay error (p. ej. inventario).
- **Pedidos:** el cuerpo de creación (propuesta) solo lleva `productId`, `size`, `quantity`, envío, método de pago y cupón; **sin precios, descuentos ni totales**. El id y el total los asigna el backend.
- **Checkout:** sin pasarela real, tarjeta/PSE se muestran como "Simulación" con aviso; en modo API se deshabilitan y contraentrega solo se habilita si C la incluye en `CHECKOUT_CONFIG.backendMethods`. Los datos de tarjeta nunca se envían, guardan ni registran, y se borran del formulario tras confirmar.

## 6. Netlify

`public/_redirects` → `/*  /index.html  200` (copiado a `dist/t-style/browser/_redirects`). `netlify.toml`: `command = "npm run build"`, `publish = "dist/t-style/browser"`, `NODE_VERSION = "22"`.

## 7. Funcionalidades que siguen con datos locales

Catálogo (12 productos demo), carrito, cupones (`TSTYLE10`, `OTONO26`), costo de envío, pedidos y checkout, panel admin (productos y estados de pedidos), y videos (6 demo; YouTube solo si se configura una clave pública). Autenticación ya usa el backend.

## 8. Endpoints pendientes de confirmación

| Responsable | Clave en `api-endpoints.ts` | Qué se necesita confirmar |
|---|---|---|
| A | `auth.*` (7 rutas actuales) | Que las rutas y el cuerpo/respuesta coinciden; que la respuesta de login trae `token, userId, name, email, role` y `/auth/me` `userId, name, email, role`; valores de `role` (`CLIENT`/`ADMIN`); formato de errores (`message`). URL pública del backend y configuración CORS. |
| B | `catalog.list`, `catalog.byId`, `catalog.search` | Rutas, DTO de producto (tipo de `id`, tallas, stock, precios), si hay paginación (`Page` de Spring o arreglo) y nombres de parámetros. |
| B | `catalogAdmin.create/update/remove` | Rutas admin, métodos HTTP y cuerpo. |
| B | `videos.list` | Servicio TMDB: ruta y DTO (título, descripción, imagen, trailer). |
| C | `cart.get/addItem/updateItem/removeItem/clear` | Rutas, cuerpos y DTO del carrito; errores de inventario (código y mensaje). |
| C | `orders.create`, `orders.mine` | Ruta, cuerpo de creación (ver `CreateOrderRequest`), DTO de pedido, métodos de pago aceptados. |
| C | `ordersAdmin.all`, `ordersAdmin.updateStatus` | Rutas, enum de estados del backend y cuerpo. |

Adaptadores a ajustar cuando lleguen los DTO: `toProduct`, `toCartLines`, `toOrder`, `toVideo`, `toPageResult`, y `CatalogService.params()`.

## 9. Resultado de `npm run build`

- En el entorno de verificación, `npm run build` sin cambios **falla** al insertar Google Fonts (`fonts.googleapis.com` devolvió 403 por la red restringida del entorno; el build original tenía la misma falla). No es un error del código; en Netlify o en un equipo con internet la descarga funciona.
- Con la inserción de fuentes desactivada **solo durante la verificación** (no quedó en `angular.json`), el build de producción terminó sin errores ni advertencias: inicial 348,52 kB (96,79 kB transferidos), dentro de los presupuestos.
- `npm run build:dev` y `ng build --configuration proxy`: correctos.
- `tsc -p tsconfig.app.json --noEmit`: sin errores de TypeScript.

## 10. Resultado de las pruebas

- `npm test`: **5 archivos, 21 pruebas, todas aprobadas** (antes: 1 archivo, 2 pruebas).
  - Detección de backend con URL relativa, absoluta y casos de suplantación de dominio/ruta.
  - Interceptor: token con `/api`, `http://localhost:8080/api` y URL absoluta de Tomcat; sin token hacia YouTube, dominios parecidos y `/api` de Netlify; 401 cierra sesión y redirige.
  - Sesión: expiración, sesión inválida, login, logout.
  - `adminGuard`: un rol ADMIN manipulado en `localStorage` es rechazado si `/auth/me` responde otro rol.
  - Modo local: ninguna petición HTTP a rutas supuestas.
- Navegación directa (build de producción servido con la regla de `_redirects` emulada y Chromium headless): `/coleccion`, `/producto/p1`, `/carrito`, `/videos`, `/checkout`, `/admin` y una ruta inexistente cargan sin errores JS; `/checkout` y `/admin` sin sesión redirigen a `/login`.
- Checkout de extremo a extremo en modo local (contraentrega): pedido creado, carrito vaciado, número de tarjeta ausente de `localStorage`.
- **No probado:** integración contra el backend real (no hay endpoints confirmados ni backend disponible en el entorno) ni un despliegue real en Netlify.

## 11. Despliegue en Netlify

1. Reemplazar la URL pendiente en `src/environments/environment.ts` por la URL real del backend.
2. Netlify → *Add new site* → importar el repositorio; *Base directory* = carpeta `t-style` (los demás valores los toma de `netlify.toml`).
3. Desplegar y comprobar recargando una ruta interna (p. ej. `/coleccion`).
4. Enviar a A el dominio exacto de Netlify para el origen permitido de CORS (`Authorization`, `Content-Type`; métodos `GET, POST, PUT, PATCH, DELETE, OPTIONS`; sin `*`).

## Observaciones

- El zip recibido incluye un `package-lock.json` vacío fuera de `t-style/` (`"name": "t-style (1)"`); conviene eliminarlo del repositorio.
- Los totales, envío y cupones del carrito se siguen calculando en Angular solo como estimado visual; el backend de C debe recalcularlos.
- Si la sesión se recupera en modo API, el carrito solo muestra productos ya cargados del catálogo.
