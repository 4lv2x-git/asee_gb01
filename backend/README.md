# Subsonic Festival — Backend

Esta es la guía de puesta en marcha de todo el backend. El backend funciona ahora como **un único stack de Docker Compose**, con un contenedor por base de datos y un contenedor por microservicio.

## Importante: dónde está realmente el código

Esta carpeta `backend/` contiene los microservicios en `components/` y algunos artefactos locales sobrantes (un `src/server.js` vacío). **Docker construye los microservicios desde `backend/components/`, no desde los antiguos compose de cada componente.**

Las fuentes reales y la definición del stack están en la raíz del repositorio:

- [`../docker-compose.yml`](../docker-compose.yml) — el stack único del backend (este es el archivo que hay que ejecutar).
- [`components/`](components/) — el código fuente y los Dockerfiles de cada microservicio.

Los archivos compose antiguos de cada componente (`components/subsonic-users-auth/docker-compose.yml`, `components/subsonic-inventory-manager/docker-compose.yml` y `components/service-artists-blog/compose.yaml`) siguen ahí como referencia/prueba, pero **no los arranques a la vez que el stack de la raíz**: publican puertos que se solapan.

## Componentes

| Componente | Stack | Qué hace | Base de datos | Puerto en el host |
| --- | --- | --- | --- | --- |
| [`subsonic-users-auth`](components/subsonic-users-auth/) | Python / FastAPI | Acceso, JWT, perfiles, administración y comprobaciones internas de roles. Crea un administrador inicial al arrancar. | MongoDB 7 (`subsonic_users`, replica set `rs0`) | `8000` |
| [`service-artists-blog`](components/service-artists-blog/) | Python / FastAPI | Fichas de artistas y artículos del blog. Carga seis artistas de demostración si la colección está vacía. Los endpoints de administración y uso interno validan el JWT firmado con el `JWT_SECRET` compartido. | MongoDB 8 (`subsonic_festival`) | `8002` |
| [`service-festivals`](components/service-festivals/) | Python / FastAPI | Festivales, escaleta y pases (`/eventos`, `/eventos/{id}/escaleta`, `/eventos/{id}/pases`). | PostgreSQL 15 (`festivals_db`) | `8001` |
| [`subsonic-inventory-manager`](components/subsonic-inventory-manager/) | Java / Spring Boot | Catálogo de productos, stock, pedidos y endpoints internos de verificación. Inicializa el catálogo al arrancar. | PostgreSQL 15 (`inventory_db`) | `8081` |
| [`subsonic-grounds`](components/subsonic-grounds/) | Python / FastAPI | Gestión de espacios y recintos (`/api/grounds`), precios y alquileres. | SQLite en su propio volumen | `8004` |

Cada base de datos tiene su **propio contenedor y su propio volumen con nombre**, de modo que los servicios siguen aislados aunque dos de ellos usen MongoDB o PostgreSQL:

| Volumen | Lo usa |
| --- | --- |
| `subsonic_backend_mongo_auth_data` | `mongo` (usuarios/auth) |
| `subsonic_backend_artists_mongo_data` | `mongo-artists` |
| `subsonic_backend_postgres_festivals_data` | `postgres-festivals` |
| `subsonic_backend_postgres_inventory_data` | `postgres-inventory` |
| `subsonic_backend_grounds_data` | `grounds` (archivo SQLite) |

Todos los contenedores comparten la red bridge `subsonic_backend`, así que los servicios se llaman entre sí por nombre de servicio (por ejemplo `mongodb://mongo:27017`), mientras que solo los puertos indicados arriba se publican en tu máquina.

## Requisitos

- Docker Engine con el plugin de Compose (`docker compose`).
- No hace falta instalar Python, Java, MongoDB ni PostgreSQL en local para el backend.
- El frontend **no** está incluido: se ejecuta fuera de Docker en el puerto `8080`.

## Arranque del backend

Ejecuta estos comandos desde la **raíz del repositorio** (la carpeta que contiene `docker-compose.yml`):

```powershell
docker compose -f docker-compose.yml config --quiet
docker compose -f docker-compose.yml up --build -d
```

La primera ejecución construye cinco imágenes (`subsonic/users-auth:local`, `subsonic/artists-blog:local`, `subsonic/festivals:local`, `subsonic/inventory-manager:local` y `subsonic/grounds:local`), crea los volúmenes y la red, arranca las bases de datos y después los servicios. El replica set de MongoDB lo inicia un contenedor de una sola ejecución, `mongo-rs-init`, que espera a que Mongo admita escrituras antes de dejar arrancar `auth`.

## Comprobación

```powershell
docker compose -f docker-compose.yml ps
```

Esperado: los contenedores de base de datos aparecen como `Up ... (healthy)` y los cinco servicios como `Up`. Si quieres ver también el contenedor de inicialización, usa:

```powershell
docker compose -f docker-compose.yml ps --all
```

`subsonic-mongo-rs-init` debe aparecer como `Exited (0)`.

Comprobaciones HTTP rápidas:

```powershell
Invoke-WebRequest http://127.0.0.1:8000/health               # auth
Invoke-WebRequest http://127.0.0.1:8002/health               # artists & blog
Invoke-WebRequest http://127.0.0.1:8001/eventos              # festivales
Invoke-WebRequest http://127.0.0.1:8004/api/grounds          # recintos
Invoke-WebRequest http://127.0.0.1:8081/inventario/productos # inventario
```

Todas deben devolver HTTP `200`. La documentación interactiva (Swagger/OpenAPI) está disponible en `/docs` en cada servicio FastAPI, y la interfaz de Springdoc en el servicio de inventario.

## Conectar el frontend

Arranca el frontend normalmente desde `../frontend`, pero apunta el gateway de eventos a `8001`, porque el puerto `8000` lo ocupa `auth`:

```powershell
$env:GATEWAY_EVENTS_URL = "http://localhost:8001"
cd frontend
mvn spring-boot:run
```

El resto de valores por defecto del frontend ya coinciden con este stack:

```text
USERS_AUTH_API_URL=http://localhost:8000
GATEWAY_ARTISTS_URL=http://localhost:8002
GATEWAY_INVENTORY_URL=http://localhost:8081
GATEWAY_GROUNDS_URL=http://localhost:8004
GATEWAY_AUTH_URL=http://localhost:8000
```

Abre `http://localhost:8080`.

Las credenciales del administrador semilla definidas en el compose son:

```text
admin@subsonicfestival.com / cambia-esto-en-produccion
```

Cambia `ADMIN_PASSWORD`, `JWT_SECRET` e `INTERNAL_API_KEY` en `docker-compose.yml` antes de cualquier uso que no sea local.

## Configuración y datos

- Toda la configuración del backend está **en línea en `../docker-compose.yml`**. No hay entradas `env_file:`; los antiguos archivos `.env.example`/`.env` no los usa este stack.
- Las credenciales de PostgreSQL son `postgres` / `postgres` en ambas bases de datos, pero cada una vive en su propio contenedor y volumen.
- `JWT_SECRET` es compartido por `auth` y `artists-blog` para que los tokens emitidos por auth sean aceptados por el servicio de artistas.
- `subsonic-users-auth` fija `beanie>=1.26,<2` en [`components/subsonic-users-auth/pyproject.toml`](components/subsonic-users-auth/pyproject.toml). Beanie 2.x no es compatible con Motor 3.x y evitaba que el servicio arrancara.
- `subsonic-grounds` incorporó un Dockerfile en [`components/subsonic-grounds/Dockerfile`](components/subsonic-grounds/Dockerfile) para que el stack de la raíz pueda construirlo.
- La opción `SPACES_SERVICE_URL` de `auth` se resuelve mediante el alias de red `subsonic-spaces` en `grounds`. La ruta heredada `/v1/interno/proveedores/{id}/espacios` no está implementada en `grounds`, así que `GET /v1/usuarios/historial-espacios` devolverá `502` hasta que esa ruta exista. No afecta al acceso ni a los flujos normales de autenticación.
- Todos los puertos publicados están ligados a `127.0.0.1`, por lo que el stack no queda expuesto a tu red local.

## Detener y reiniciar

```powershell
# Detener y quitar contenedores y red, CONSERVANDO los datos
docker compose -f docker-compose.yml down

# Detener y quitar contenedores, red y TODOS los volúmenes (arranque limpio)
docker compose -f docker-compose.yml down -v

# Reconstruir un solo servicio tras cambiar su código o Dockerfile
docker compose -f docker-compose.yml up --build -d auth
```

## Validación ya realizada

El stack de la raíz se construyó y arrancó desde cero. Las cinco APIs respondieron `200` en las comprobaciones anteriores, los cuatro contenedores de base de datos alcanzaron el estado `healthy` y el contenedor de inicialización del replica set de MongoDB se comportó de forma idempotente al volver a ejecutarse. Después se detuvieron los contenedores con `down` (los volúmenes se conservan), así que puedes partir de los comandos de **Arranque del backend**.
