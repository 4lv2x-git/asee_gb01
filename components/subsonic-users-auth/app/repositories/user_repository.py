from datetime import datetime, timezone
from typing import Any

from beanie import PydanticObjectId

from app.models.enums import Estado, Rol
from app.models.user import User


def normalize_email(email: str) -> str:
    return email.lower().strip()


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


class UserRepository:
    async def create(
        self,
        *,
        nombre: str,
        correo: str,
        password_hash: str,
        rol: Rol,
        estado: Estado = Estado.ACTIVO,
        email_verificado: bool = False,
        direccion: str | None = None,
        tipo_servicio: str | None = None,
        descripcion_servicio: str | None = None,
    ) -> User:
        user = User(
            nombre=nombre,
            correo=correo,
            correo_normalizado=normalize_email(correo),
            password_hash=password_hash,
            rol=rol,
            estado=estado,
            email_verificado=email_verificado,
            direccion=direccion,
            tipo_servicio=tipo_servicio,
            descripcion_servicio=descripcion_servicio,
        )
        return await user.insert()

    async def get_by_email(self, correo: str) -> User | None:
        return await User.find_one(User.correo_normalizado == normalize_email(correo))

    async def get_by_id(self, user_id: str) -> User | None:
        if not PydanticObjectId.is_valid(user_id):
            return None
        return await User.get(PydanticObjectId(user_id))

    async def list_all(self) -> list[User]:
        return await User.find_all().to_list()

    async def update(self, user: User, data: dict[str, Any]) -> User:
        for key, value in data.items():
            setattr(user, key, value)
        user.updated_at = utc_now()
        await user.save()
        return user

    async def delete(self, user: User) -> None:
        await user.delete()

    async def bump_token_version(self, user: User) -> User:
        user.token_version += 1
        user.updated_at = utc_now()
        await user.save()
        return user
