# Microservicio de Festivales (Festivals Service) - Subsonic Festival

Este microservicio se encarga de la gestión y mantenimiento del catálogo de festivales dentro de la arquitectura de microservicios de la aplicación **Subsonic Festival**. Ha sido desarrollado utilizando **Python (FastAPI)** como API REST y **PostgreSQL** como base de datos relacional.

---

## Tecnologías Utilizadas

* **Lenguaje:** Python 3.9+
* **Framework Web:** FastAPI (con Uvicorn como servidor ASGI)
* **Base de Datos:** PostgreSQL 15
* **ORM:** SQLAlchemy
* **Validación de Datos:** Pydantic
* **Contenedorización:** Docker

---

## Requisitos Previos

Antes de ejecutar el microservicio, asegúrate de tener instalado en tu sistema:
1. [Python 3.9+](https://www.python.org/downloads/)
2. [Docker Desktop](https://www.docker.com/products/docker-desktop/) (para la base de datos)
3. Git

---

## Pasos y Comandos para Ejecutar el Componente

Sigue estos pasos en tu terminal (PowerShell en Windows o Bash en Linux/macOS) en el orden indicado:

### 1. Iniciar la Base de Datos con Docker
Ejecuta el contenedor de PostgreSQL con las credenciales y el puerto necesarios:

```bash
docker run --name postgres-festivals -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=festivals_db -p 5433:5432 -d postgres:15
```

### 2. Activar el Entorno Virtual
Desde la raíz del repositorio (`asee_gb01`), activa el entorno virtual de Python:

* **Windows (PowerShell):**
  ```powershell
  .\venv\Scripts\Activate.ps1
  ```
* **Linux/macOS:**
  ```bash
  source venv/bin/activate
  ```

### 3. Navegar a la Carpeta del Microservicio
```bash
cd components/service-festivals
```

### 4. Instalar Dependencias
Asegúrate de que el archivo `requirements.txt` esté actualizado e instala todas las librerías necesarias:

```bash
pip install -r requirements.txt
```

### 5. Lanzar el Servidor con Uvicorn
Ejecuta el servidor de desarrollo en el puerto 8000:

```bash
uvicorn src.main:app --reload --port 8000
```

---

## Comprobación y Pruebas

Una vez arrancado el servidor, abre tu navegador web e introduce la siguiente dirección para acceder a la interfaz interactiva de Swagger UI:

-> **[http://localhost:8000/docs](http://localhost:8000/docs)**

Desde esta interfaz podrás realizar pruebas de las operaciones CRUD disponibles:
* **`GET /api/festivals`**: Obtener el listado de festivales.
* **`POST /api/festivals`**: Crear un nuevo festival.
* **`GET /api/festivals/{festival_id}`**: Consultar un festival por su ID.
* **`DELETE /api/festivals/{festival_id}`**: Eliminar un festival existente.

---

## Estructura del Proyecto

```text
components/service-festivals/
├── src/
│   ├── __init__.py
│   ├── database.py       # Configuración de la conexión a PostgreSQL con SQLAlchemy
│   ├── models.py         # Modelo de datos ORM de la tabla 'festivals'
│   ├── schemas.py        # Esquemas de validación de entradas y salidas con Pydantic
│   ├── crud.py           # Funciones de consulta y persistencia en base de datos
│   └── main.py           # Definición de rutas y endpoints del API de FastAPI
├── Dockerfile            # Configuración para contenedorizar el microservicio
└── requirements.txt      # Dependencias de Python necesarias
```