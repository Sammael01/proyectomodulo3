# T Style · Backend

Java 21 + Spring Boot + Maven, empaquetado como WAR para Tomcat.
Este README documenta el **módulo B (catálogo e integraciones externas)**.

## Variables de entorno

| Variable | Obligatoria | Uso |
| --- | --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Sí | Conexión a MySQL |
| `TMDB_ACCESS_TOKEN` | Solo para `/api/movies` | Token de lectura (Bearer) de TMDB |
| `CATALOG_IMPORT_ENABLED` | No (por defecto `false`) | Activa la importación del catálogo al arrancar |
| `CATALOG_IMPORT_PATH` | Si se activa la importación | `file:/ruta/catalogo.json` o `classpath:...` |

`application.properties` no contiene secretos y mantiene `spring.jpa.hibernate.ddl-auto=none`:
el esquema lo genera el mecanismo del profesor a partir de su matriz JSON.

## Endpoints del catálogo

Todos son `GET`, públicos y paginados.

| Ruta | Descripción |
| --- | --- |
| `/api/products` | Parámetros: `categoryId`, `providerId`, `search` (máx. 100), `page` (desde 0), `size` (1–50, por defecto 12), `sortBy` (`name`, `price`, `id`), `direction` (`asc`/`desc`) |
| `/api/products/{id}` | Ficha de un producto; 404 si no existe o su proveedor está inactivo |
| `/api/movies?query=&page=` | Búsqueda en TMDB (2–100 caracteres, páginas 0–19, caché de 10 min) |
| `/api/movies/{id}` | Detalle con `trailerUrl` de YouTube si TMDB lo informa (caché de 1 h) |

### Contrato `ProductResponse` (consumido por wishlist y carrito)

```json
{
  "id": 5,
  "provider": "NombreProveedor",
  "externalId": "ID-EN-EL-PROVEEDOR",
  "name": "Nombre",
  "imageUrl": "https://...",
  "price": 59900.00,
  "currency": "COP",
  "externalUrl": "https://...",
  "providerAvailable": true
}
```

- `price` y `currency` son los valores originales del proveedor; no se convierte moneda.
- `providerAvailable` es la disponibilidad **informada por el proveedor** y puede ser `null`
  si no la informa. No es stock interno.
- Un producto se identifica por **proveedor + externalId**, nunca por título.
- El campo se llamaba `providerAvalaible` (typo); ahora es `providerAvailable`.
  El nombre de la columna en BD (`provider_avalaible`) no se tocó.

Errores: se usan `BusinessException` (400), `ResourceNotFoundException` (404) y
`ExternalServiceException` (503) a través de `GlobalExceptionHandler`.

## Importar el JSON de datos del catálogo

Es un JSON de **registros de productos**, distinto del JSON de matriz/esquema del profesor.
Formato (una lista):

```json
[
  {
    "provider": "NombreProveedor",
    "externalId": "ID-1",
    "name": "Nombre",
    "description": "Opcional",
    "imageUrl": "https://...",
    "price": 59900,
    "currency": "COP",
    "externalUrl": "https://...",
    "category": "Camisetas",
    "providerAvailable": true
  }
]
```

Para ejecutarlo: `CATALOG_IMPORT_ENABLED=true CATALOG_IMPORT_PATH=file:/ruta/catalogo.json`
y arrancar la aplicación. Cada registro se valida; los inválidos se omiten, se registran en el
log con su posición y no detienen los demás. Reimportar actualiza (no duplica) por proveedor + externalId.
Las tablas deben existir antes (generadas por el mecanismo del profesor).

## Pruebas

```
./mvnw clean verify
```

`DatabaseConnectionTest` solo corre si existe `DB_URL`. Las demás usan mocks y datos controlados
(`src/test/resources/catalog`).

## Bloqueos y pendientes documentados

- **Matriz JSON del profesor**: no se ha cotejado con las entidades `Product`, `Provider` y `Category`.
  Hacerlo antes de generar la base de datos.
- **Proveedor de ropa real**: no hay credenciales ni acceso autorizado verificado. Existe la interfaz
  `ProviderAdapter` para conectarlo sin acoplar el catálogo; mientras tanto la fuente es la
  importación JSON validada (`JsonCatalogAdapter`). No se afirma que haya integración real con ningún proveedor.
- **Endpoint de importación por HTTP**: no se expone porque aún no existe autorización por rol ADMIN
  (módulo A). Por eso la importación corre solo al arrancar.
- **Tráileres**: provienen de los videos de YouTube que informa TMDB; no se usa la API de YouTube.
- **Caché del catálogo externo / límites por proveedor**: la caché en memoria aplica a películas;
  los productos se sirven desde la base de datos sincronizada (`lastSyncedAt`).
