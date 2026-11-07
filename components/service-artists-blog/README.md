# Artists & Blog

Microservicio de artistas y blog desarrollado con Python, FastAPI y MongoDB.

## Requisitos

- Python 3.10 o posterior
- MongoDB disponible en `MONGODB_URL`

## Ejecutar localmente

Desde esta carpeta:

```powershell
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
$env:MONGODB_URL = "mongodb://localhost:27017"
$env:MONGODB_DATABASE = "subsonic_festival"
.\.venv\Scripts\python.exe -m uvicorn src.main:app --reload --port 8000
```

La documentación interactiva está en <http://localhost:8000/docs>. Las variables
de entorno también están descritas en `.env.example`. Los endpoints
administrativos y de uso interno requieren `JWT_SECRET` y un token con el rol
correspondiente.

## Ejecutar con Docker

Construye la imagen desde esta carpeta:

```powershell
docker build -t artists-blog .
```

Ejecuta el servicio, configurando `MONGODB_URL` con una dirección de MongoDB
accesible desde el contenedor:

```powershell
docker run --rm -p 8000:8000 -e MONGODB_URL="mongodb://host.docker.internal:27017" -e MONGODB_DATABASE="subsonic_festival" artists-blog
```

## Pruebas

Desde esta carpeta, instala las dependencias y ejecuta:

```powershell
.\.venv\Scripts\python.exe -m unittest discover -s tests
```

La prueba `test_openapi_contract.py` compara las operaciones implementadas con
los contratos del repositorio:

- `../../docs/openapi/artistas.yml`
- `../../docs/openapi/blog.yml`

## Endpoints del contrato

| Método | Ruta |
| --- | --- |
| GET | `/v1/artistas` |
| GET | `/v1/artistas/{artista_id}` |
| GET | `/v1/artistas/{artista_id}/canciones` |
| POST | `/v1/artistas/interno/lote` |
| POST | `/v1/artistas/admin` |
| PUT | `/v1/artistas/admin/{artista_id}` |
| DELETE | `/v1/artistas/admin/{artista_id}` |
| GET | `/v1/blog` |
| GET | `/v1/blog/{articulo_id}` |
