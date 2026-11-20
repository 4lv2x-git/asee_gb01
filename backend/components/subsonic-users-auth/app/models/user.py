from datetime import datetime, timezone
from typing import Optional

from beanie import Document
from pydantic import Field

from app.models.enums import Estado, Rol


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


class User(Document):
    nombre: str
    correo: str
    correo_normalizado: str
    password_hash: str
    rol: Rol
    estado: Estado = Estado.ACTIVO
    email_verificado: bool = False
    direccion: Optional[str] = None
    tipo_servicio: Optional[str] = None
    descripcion_servicio: Optional[str] = None
    token_version: int = 0
    created_at: datetime = Field(default_factory=utc_now)
    updated_at: datetime = Field(default_factory=utc_now)

    class Settings:
        name = "usuarios"
