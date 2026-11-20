from typing import Annotated

from fastapi import APIRouter, Depends, Response, status

from app.api.deps import require_admin
from app.models.user import User
from app.schemas.admin import AdminPasswordReset, EstadoUpdate
from app.schemas.common import MessageResponse
from app.schemas.user import Usuario, UsuarioEdicion, user_to_schema
from app.services.admin_service import AdminService

router = APIRouter(prefix="/usuarios", tags=["Administracion"])


@router.get("/admin", response_model=list[Usuario])
async def list_users(_admin: Annotated[User, Depends(require_admin)]) -> list[Usuario]:
    return [user_to_schema(user) for user in await AdminService().list_users()]


@router.put("/admin/{user_id}", response_model=Usuario)
async def update_user(
    user_id: str,
    data: UsuarioEdicion,
    _admin: Annotated[User, Depends(require_admin)],
) -> Usuario:
    user = await AdminService().update_user(user_id, data)
    return user_to_schema(user)


@router.delete("/admin/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_user(user_id: str, _admin: Annotated[User, Depends(require_admin)]) -> Response:
    await AdminService().delete_user(user_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.patch("/admin/{user_id}/estado", response_model=Usuario)
async def update_estado(
    user_id: str,
    data: EstadoUpdate,
    _admin: Annotated[User, Depends(require_admin)],
) -> Usuario:
    user = await AdminService().update_estado(user_id, data)
    return user_to_schema(user)


@router.put("/admin-password-reset/{user_id}", response_model=MessageResponse)
async def reset_password(
    user_id: str,
    data: AdminPasswordReset,
    _admin: Annotated[User, Depends(require_admin)],
) -> MessageResponse:
    await AdminService().reset_password(user_id, data)
    return MessageResponse(message="Password modificada correctamente")
