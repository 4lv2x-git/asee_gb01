# subsonic-users-auth

Microservicio FastAPI para autenticacion, autorizacion y gestion de usuarios de Subsonic Festival.

## Arranque local

```bash
cd backend/src/subsonic-users-auth
cp .env.example .env
docker compose up --build
```

La API queda disponible en `http://localhost:8000`; `/health` responde fuera del prefijo y los endpoints de negocio viven bajo `/v1`.

## Variables principales

- `MONGODB_URI`, `MONGODB_DB`: conexion a MongoDB.
- `JWT_SECRET`, `JWT_ALGORITHM`, `ACCESS_TOKEN_EXPIRE_MINUTES`: firma y caducidad de JWT.
- `INTERNAL_API_KEY`: clave para endpoints internos y llamadas al gestor de espacios.
- `SPACES_SERVICE_URL`: URL base del gestor de espacios.
- `ADMIN_EMAIL`, `ADMIN_PASSWORD`: credenciales del admin semilla.
- `CORS_ORIGINS`: origenes permitidos del frontend.

## Seed admin

El lifespan de FastAPI crea un administrador inicial de forma idempotente. Tambien puede ejecutarse manualmente:

```bash
python scripts/seed_admin.py
```

## Lagunas del contrato documentadas

Resueltas:
- ~~`direccion` se guarda y se devuelve en `Usuario` para mantener coherencia con `UsuarioEdicion`.~~
- ~~`forgot-password` genera codigo de recuperacion, pero el contrato no define endpoint publico para consumirlo.~~ -> `POST /auth/forgot-password`
- ~~`GET /usuarios/{user_id}/verificar-rol` exige `X-Internal-Key`, aunque el YAML no lo declare.~~ -> Creada  `InternalApiKey`.
- ~~`POST /auth/login` devuelve `TokenResponse {accessToken, tokenType, expiresIn}`.~~ -> `POST /auth/login` devuelve `TokenResponse {accessToken, tokenType, expiresIn}`

- `POST /auth/reset-password` incrementa `token_version` al cambiar la contraseña y revoca los tokens anteriores del usuario.

Por resolver:
- `POST /auth/forgot-password` y `POST /auth/reset-password` no tienen rate limit.

## Tests

```bash
pip install -e ".[test]"
pytest
```
