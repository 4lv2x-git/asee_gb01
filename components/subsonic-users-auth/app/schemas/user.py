from pydantic import EmailStr, Field, model_validator

from app.models.enums import Estado, Rol
from app.schemas.common import CamelModel


class Usuario(CamelModel):
    id: str
    nombre: str
    correo: EmailStr
    rol: Rol
    estado: Estado
    email_verificado: bool
    direccion: str | None = None
    tipo_servicio: str | None = None
    descripcion_servicio: str | None = None


class UsuarioRegistro(CamelModel):
    nombre: str = Field(min_length=1)
    correo: EmailStr
    password: str = Field(min_length=8)
    rol: Rol
    tipo_servicio: str | None = None
    descripcion_servicio: str | None = None

    @model_validator(mode="after")
    def validate_role(self):
        if self.rol not in {Rol.CLIENTE, Rol.PROVEEDOR}:
            raise ValueError("Solo se permite registrar cliente o proveedor")
        if self.rol == Rol.PROVEEDOR and not self.tipo_servicio:
            raise ValueError("tipoServicio es obligatorio para proveedores")
        return self


class UsuarioEdicion(CamelModel):
    nombre: str | None = Field(default=None, min_length=1)
    direccion: str | None = None
    tipo_servicio: str | None = None
    descripcion_servicio: str | None = None


def user_to_schema(user) -> Usuario:
    return Usuario(
        id=str(user.id),
        nombre=user.nombre,
        correo=user.correo,
        rol=user.rol,
        estado=user.estado,
        email_verificado=user.email_verificado,
        direccion=user.direccion,
        tipo_servicio=user.tipo_servicio,
        descripcion_servicio=user.descripcion_servicio,
    )
