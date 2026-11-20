from typing import Annotated

from fastapi import APIRouter, Depends, Response, status

from app.api.deps import get_current_user, get_spaces_client, get_token_payload, require_proveedor
from app.clients.spaces_client import SpacesClient
from app.models.user import User
from app.schemas.user import Usuario, UsuarioEdicion, user_to_schema
from app.services.auth_service import AuthService
from app.services.user_service import UserService

router = APIRouter(prefix="/usuarios", tags=["Mi Perfil (Cliente / Proveedor)"])


@router.get("/perfil", response_model=Usuario)
async def get_profile(user: Annotated[User, Depends(get_current_user)]) -> Usuario:
    return user_to_schema(user)


@router.patch("/perfil", response_model=Usuario)
async def patch_profile(user: Annotated[User, Depends(get_current_user)], data: UsuarioEdicion) -> Usuario:
    updated = await UserService().update_profile(user, data)
    return user_to_schema(updated)


@router.delete("/perfil", status_code=status.HTTP_204_NO_CONTENT)
async def delete_profile(
    user: Annotated[User, Depends(get_current_user)],
    payload: Annotated[dict, Depends(get_token_payload)],
) -> Response:
    await AuthService().logout(payload)
    await UserService().delete_profile(user)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.get("/historial-espacios")
async def historial_espacios(
    user: Annotated[User, Depends(require_proveedor)],
    spaces_client: Annotated[SpacesClient, Depends(get_spaces_client)],
) -> list[dict]:
    return await spaces_client.get_espacios_by_proveedor(str(user.id))
