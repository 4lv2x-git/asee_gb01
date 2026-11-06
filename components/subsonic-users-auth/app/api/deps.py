import secrets
from datetime import datetime, timezone
from typing import Annotated, Callable

from fastapi import Depends, Header
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from app.clients.spaces_client import SpacesClient
from app.core.config import get_settings
from app.core.exceptions import ForbiddenError, UnauthorizedError
from app.core.security import decode_token
from app.models.enums import Estado, Rol
from app.models.user import User
from app.repositories.revoked_token_repository import RevokedTokenRepository
from app.repositories.user_repository import UserRepository

bearer = HTTPBearer(auto_error=False)


async def get_token_payload(
    credentials: Annotated[HTTPAuthorizationCredentials | None, Depends(bearer)],
) -> dict:
    if not credentials:
        raise UnauthorizedError("Token no recibido")
    payload = decode_token(credentials.credentials)
    if "exp" not in payload or datetime.fromtimestamp(int(payload["exp"]), timezone.utc) < datetime.now(timezone.utc):
        raise UnauthorizedError("Token expirado")
    return payload


async def get_current_user(payload: Annotated[dict, Depends(get_token_payload)]) -> User:
    jti = payload.get("jti")
    user_id = payload.get("sub")
    if not jti or not user_id:
        raise UnauthorizedError("Token invalido")
    if await RevokedTokenRepository().exists(jti):
        raise UnauthorizedError("Token revocado")
    user = await UserRepository().get_by_id(user_id)
    if not user:
        raise UnauthorizedError("Usuario no encontrado")
    if user.estado != Estado.ACTIVO:
        raise ForbiddenError("Usuario suspendido")
    if payload.get("token_version", 0) != user.token_version:
        raise UnauthorizedError("Token revocado")
    return user


def require_roles(*roles: Rol) -> Callable:
    async def dependency(user: Annotated[User, Depends(get_current_user)]) -> User:
        if user.rol not in roles:
            raise ForbiddenError("Rol insuficiente")
        return user

    return dependency


require_admin = require_roles(Rol.ADMIN)
require_proveedor = require_roles(Rol.PROVEEDOR)


async def require_internal(x_internal_key: Annotated[str | None, Header(alias="X-Internal-Key")] = None) -> None:
    if not x_internal_key or not secrets.compare_digest(x_internal_key, get_settings().internal_api_key):
        raise ForbiddenError("Clave interna invalida")


def get_spaces_client() -> SpacesClient:
    return SpacesClient()
