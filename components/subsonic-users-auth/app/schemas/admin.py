from pydantic import Field

from app.models.enums import Estado
from app.schemas.common import CamelModel


class EstadoUpdate(CamelModel):
    estado: Estado


class AdminPasswordReset(CamelModel):
    nueva_password: str = Field(min_length=8)
