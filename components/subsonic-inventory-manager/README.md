# Microservicio Gestor de Inventario y Pedidos (Inventory Manager) - Subsonic Festival

Este microservicio se encarga del control de disponibilidad de entradas, pases y productos de merchandising, así como del registro y procesamiento de las compras realizadas por los clientes en la arquitectura de **Subsonic Festival**. Ha sido desarrollado utilizando **Java 17**, el framework **Spring Boot 3** y **PostgreSQL** como Sistema Gestor de Bases de Datos (SGBD).

---

## Tecnologías Utilizadas

* **Lenguaje:** Java 17
* **Framework:** Spring Boot 3.2.5 (Spring Web, Spring Data JPA, Spring Validation)
* **Base de Datos:** PostgreSQL 15
* **ORM:** Hibernate / JPA
* **Documentación de API:** SpringDoc OpenAPI v2 (Swagger UI / OpenAPI 3.0.3)
* **Librerías de Utilidad:** Lombok
* **Contenedorización:** Docker y Docker Compose

---

## Requisitos Previos

Antes de ejecutar el microservicio, asegúrate de tener instalado en tu sistema:
1. [Java Development Kit (JDK) 17+](https://adoptium.net/)
2. [Apache Maven 3.8+](https://maven.apache.org/download.cgi)
3. [Docker Desktop](https://www.docker.com/products/docker-desktop/) (para alojar y ejecutar la base de datos PostgreSQL)
4. Git

---

## Pasos y Comandos para Ejecutar el Componente

Sigue estos pasos en tu terminal (PowerShell en Windows o Bash en Linux/macOS) en el orden indicado:

### 1. Iniciar la Base de Datos con Docker

Ejecuta el contenedor de PostgreSQL con las credenciales y el puerto asignados para el gestor de inventario (puerto `5434` en el host para no colisionar con otros microservicios):

```bash
docker run --name postgres-inventory -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=inventory_db -p 5434:5432 -d postgres:15
```

*(Opcional: Si prefieres levantar tanto la base de datos como el servicio completo en Docker, puedes usar `docker-compose up -d`).*

### 2. Navegar a la Carpeta del Microservicio

Desde la raíz del repositorio (`asee_gb01`), entra al directorio del componente:

```bash
cd components/subsonic-inventory-manager
```

### 3. Compilar y Ejecutar la Aplicación

Ejecuta la aplicación utilizando Maven:

```bash
mvn spring-boot:run
```

O si prefieres empaquetar el `.jar` ejecutable:

```bash
mvn clean package -DskipTests
java -jar target/inventory-manager-1.0.0.jar
```

---

## Comprobación y Pruebas en Swagger UI

Una vez arrancado el servidor en el puerto **8081**, abre tu navegador web y accede a la interfaz interactiva de Swagger UI:

-> **[http://localhost:8081/docs](http://localhost:8081/docs)**

Desde esta interfaz podrás interactuar y validar todos los endpoints descritos en el contrato OpenAPI:

### 1. Inventario (`/inventario/productos`)
* **`POST /inventario/productos`**: Registrar un nuevo producto en el catálogo asignando identificador único en el backend.
* **`GET /inventario/productos`**: Consultar catálogo de productos con filtros opcionales (`tipo`, `disponible`).
* **`GET /inventario/productos/{producto_id}`**: Consultar detalle y stock de un producto específico.
* **`PATCH /inventario/productos/{producto_id}`**: Actualizar parcialmente los datos descriptivos o comerciales del producto (nombre, descripción, precio, tipo).
* **`DELETE /inventario/productos/{producto_id}`**: Eliminar un producto del catálogo garantizando la integridad de pedidos existentes.
* **`PATCH /inventario/productos/{producto_id}/stock`**: Modificar la cantidad total de stock disponible.

### 2. Pedidos (`/pedidos`)
* **`POST /pedidos`**: Crear un pedido a partir de las líneas seleccionadas por el cliente, validando roles, existencias y reduciendo el stock de manera atómica.
* **`GET /pedidos`**: Consultar el historial de pedidos asociados al cliente autenticado.
* **`GET /pedidos/{pedido_id}`**: Consultar el detalle de un pedido propio del usuario.
* **`POST /pedidos/{pedido_id}/cancelacion`**: Solicitar la cancelación de un pedido propio y de sus entradas asociadas.

### 3. Entradas y Cancelaciones (`/entradas`)
* **`GET /entradas/{entrada_id}`**: Consultar los datos de una entrada o pase adquirido.
* **`POST /entradas/{entrada_id}/cancelacion`**: Registrar una solicitud de cancelación para una entrada comprada.

### 4. Uso Interno (`/interno`)
* **`GET /interno/usuarios/{user_id}/verificar-cliente`**: Comprobar si un usuario posee rol de cliente antes de autorizar un pedido.
* **`GET /interno/eventos/{evento_id}/pases/{pase_id}`**: Validar la existencia de un festival y un pase asociado.

---

## Estructura del Proyecto

```text
components/inventory_manager/
├── pom.xml                                      # Descriptor de dependencias y plugins de Maven
├── Dockerfile                                   # Construcción multi-etapa de la imagen Docker
├── docker-compose.yml                           # Orquestación de PostgreSQL y la aplicación
├── README.md                                    # Guía de instalación, ejecución y uso
└── src/
    ├── main/
    │   ├── java/com/subsonicfestival/inventory/
    │   │   ├── InventoryApplication.java       # Clase principal que arranca Spring Boot
    │   │   ├── config/
    │   │   │   ├── DataInitializer.java        # Poblado inicial automático de productos
    │   │   │   ├── OpenApiConfig.java          # Configuración de OpenAPI 3 / Swagger UI y OAuth2
    │   │   │   └── UserContextHelper.java      # Extracción y validación del usuario autenticado
    │   │   ├── controller/
    │   │   │   ├── InventarioController.java   # Endpoints de consulta y stock de productos
    │   │   │   ├── PedidoController.java       # Endpoints de creación e historial de pedidos
    │   │   │   ├── EntradaController.java      # Endpoints de consulta y cancelación de entradas
    │   │   │   └── InternoController.java      # Endpoints de comunicación entre microservicios
    │   │   ├── dto/
    │   │   │   ├── ProductoDTO.java            # Transferencia de datos de producto
    │   │   │   ├── ProductoCreacionDTO.java    # Solicitud de creación de producto
    │   │   │   ├── ProductoActualizacionDTO.java # Solicitud de actualización de datos de producto
    │   │   │   ├── StockActualizacionDTO.java  # Solicitud de actualización de stock
    │   │   │   ├── LineaPedidoCreacionDTO.java # Entrada de línea de pedido
    │   │   │   ├── PedidoCreacionDTO.java      # Solicitud de creación de pedido
    │   │   │   ├── LineaPedidoDTO.java         # Detalle de línea de pedido
    │   │   │   ├── EntradaAdquiridaDTO.java    # Detalle de entrada/pase adquirido
    │   │   │   ├── PedidoDTO.java              # Detalle completo de pedido
    │   │   │   ├── ComprobacionRolDTO.java     # Respuesta de verificación de rol
    │   │   │   ├── ComprobacionPaseDTO.java    # Respuesta de verificación de evento/pase
    │   │   │   └── ErrorDTO.java               # Estructura uniforme de errores
    │   │   ├── entity/
    │   │   │   ├── Producto.java               # Entidad JPA para tabla 'productos'
    │   │   │   ├── TipoProducto.java           # Enumeración (ENTRADA, PASE, MERCHANDISING)
    │   │   │   ├── Pedido.java                 # Entidad JPA para tabla 'pedidos'
    │   │   │   ├── LineaPedido.java            # Entidad JPA para tabla 'lineas_pedido'
    │   │   │   └── EntradaAdquirida.java       # Entidad JPA para tabla 'entradas_adquiridas'
    │   │   ├── exception/
    │   │   │   ├── AppException.java           # Excepción base con código y estado HTTP
    │   │   │   ├── ResourceNotFoundException.java # Error 404 recurso no encontrado
    │   │   │   ├── InsufficientStockException.java # Error 409 stock insuficiente
    │   │   │   ├── ConflictException.java      # Error 409 conflicto de estado
    │   │   │   ├── UnauthorizedException.java  # Error 401 token no recibido o inválido
    │   │   │   ├── ForbiddenException.java     # Error 403 permisos insuficientes / no cliente
    │   │   │   ├── BadRequestException.java    # Error 400 datos o parámetros no válidos
    │   │   │   └── GlobalExceptionHandler.java # Manejador global que retorna ErrorDTO
    │   │   ├── repository/
    │   │   │   ├── ProductoRepository.java     # Acceso a datos de productos
    │   │   │   ├── PedidoRepository.java       # Acceso a datos de pedidos
    │   │   │   ├── LineaPedidoRepository.java  # Acceso a datos de líneas de pedido
    │   │   │   └── EntradaAdquiridaRepository.java # Acceso a datos de entradas
    │   │   └── service/
    │   │       ├── InventarioService.java      # Interfaz de lógica de inventario
    │   │       ├── InventarioServiceImpl.java  # Implementación de lógica de inventario
    │   │       ├── PedidoService.java          # Interfaz de lógica de pedidos
    │   │       ├── PedidoServiceImpl.java      # Implementación con control atómico de stock
    │   │       ├── EntradaService.java         # Interfaz de gestión de entradas
    │   │       ├── EntradaServiceImpl.java     # Implementación con solicitudes de cancelación
    │   │       ├── InternoService.java         # Interfaz de validaciones internas
    │   │       └── InternoServiceImpl.java     # Implementación de validaciones internas
    │   └── resources/
    │       └── application.properties          # Configuración de puerto, PostgreSQL y JPA
    └── test/
        ├── java/com/subsonicfestival/inventory/
        │   ├── InventoryApplicationTests.java  # Prueba de carga de contexto Spring
        │   └── service/
        │       ├── InventarioServiceTest.java  # Pruebas unitarias de inventario
        │       └── PedidoServiceTest.java      # Pruebas unitarias de pedidos y stock
        └── resources/
            └── application.properties          # Configuración para pruebas con base de datos H2
```
