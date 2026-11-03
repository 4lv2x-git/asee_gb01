# asee_gb01

## Artists & Blog

El componente se implementa como un servicio independiente en **Python con
FastAPI y MongoDB**. La API y la lógica de negocio están separadas de los
repositorios que acceden a MongoDB; ningún otro componente debe conectarse
directamente a sus colecciones.

### Ejecutar en local

Requiere Python 3.10+ y MongoDB:

```powershell
cd backend
py -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
$env:MONGODB_URL = "mongodb://localhost:27017"
$env:MONGODB_DATABASE = "subsonic_festival"
uvicorn src.main:app --reload --port 8000
```

La documentación interactiva de la API está en `http://localhost:8000/docs`.
Las variables disponibles también se describen en `backend/.env.example`.
Para activar los endpoints protegidos hay que configurar `JWT_SECRET` con la
clave compartida con el servicio de autenticación y `JWT_ALGORITHM` con el
algoritmo de firma acordado.

Las pruebas unitarias del servicio se ejecutan desde `backend` con
`py -m unittest discover -s tests`.

### Contratos HTTP

| Método | Ruta | Acceso |
| --- | --- | --- |
| GET | `/v1/artistas` | Público |
| GET | `/v1/artistas/{artista_id}` | Público |
| GET | `/v1/artistas/{artista_id}/canciones` | Público |
| POST | `/v1/artistas/interno/lote` | Rol `servicio_interno` |
| POST | `/v1/artistas/admin` | Rol `admin` |
| PUT | `/v1/artistas/admin/{artista_id}` | Rol `admin` |
| DELETE | `/v1/artistas/admin/{artista_id}` | Rol `admin` |
| GET | `/v1/blog` | Público; admite `skip` y `limit` |
| GET | `/v1/blog/{articulo_id}` | Público |
| GET | `/health` | Comprueba MongoDB |

El contrato de artistas está en [docs/openapi/artistas.yml](docs/openapi/artistas.yml)
y el de blog en [docs/openapi/blog.yml](docs/openapi/blog.yml). El servicio lee
las colecciones `artistas` y `articulos`. Los documentos deben incluir los
campos definidos en dichos contratos; el identificador se puede almacenar como
`_id` de MongoDB o como `id`.

**Miembros del grupo:**
- Jaime Arias Rodríguez
- Manuel Carvajal Tarifa
- Justo Cilleros Rodríguez
- Javier García Espina
- Álvaro Martín Rodríguez
