from app.schemas.common import CamelModel


class RolVerificacionResponse(CamelModel):
    id: str
    rol_valido: bool
