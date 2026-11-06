from typing import Annotated

from fastapi import APIRouter, Depends, Query

from app.api.deps import require_internal
from app.core.exceptions import ValidationError
from app.models.enums import Rol
from app.schemas.internal import RolVerificacionResponse
from app.services.internal_service import InternalService

router = APIRouter(prefix="/usuarios", tags=["Uso Interno"])


@router.get(
    "/{user_id}/verificar-rol",
    response_model=RolVerificacionResponse,
    dependencies=[Depends(require_internal)],
)
async def verificar_rol(
    user_id: str,
    _internal: Annotated[None, Depends(require_internal)],
    rol_esperado: Annotated[Rol, Query(alias="rolEsperado")],
) -> RolVerificacionResponse:
    if rol_esperado not in {Rol.CLIENTE, Rol.PROVEEDOR}:
        raise ValidationError("rolEsperado debe ser cliente o proveedor")
    rol_valido = await InternalService().verificar_rol(user_id, rol_esperado)
    return RolVerificacionResponse(id=user_id, rol_valido=rol_valido)
