from app.core.exceptions import NotFoundError
from app.core.security import hash_password
from app.models.enums import Estado
from app.models.user import User
from app.repositories.user_repository import UserRepository
from app.schemas.admin import AdminPasswordReset, EstadoUpdate
from app.schemas.user import UsuarioEdicion


class AdminService:
    def __init__(self, users: UserRepository | None = None):
        self.users = users or UserRepository()

    async def list_users(self) -> list[User]:
        return await self.users.list_all()

    async def update_user(self, user_id: str, data: UsuarioEdicion) -> User:
        user = await self._get(user_id)
        return await self.users.update(user, data.model_dump(exclude_unset=True, by_alias=False))

    async def delete_user(self, user_id: str) -> None:
        user = await self._get(user_id)
        await self.users.bump_token_version(user)
        await self.users.delete(user)

    async def update_estado(self, user_id: str, data: EstadoUpdate) -> User:
        user = await self._get(user_id)
        update_data = {"estado": data.estado}
        if data.estado == Estado.SUSPENDIDO:
            user = await self.users.bump_token_version(user)
        return await self.users.update(user, update_data)

    async def reset_password(self, user_id: str, data: AdminPasswordReset) -> User:
        user = await self._get(user_id)
        user = await self.users.bump_token_version(user)
        return await self.users.update(user, {"password_hash": hash_password(data.nueva_password)})

    async def _get(self, user_id: str) -> User:
        user = await self.users.get_by_id(user_id)
        if not user:
            raise NotFoundError("Usuario no encontrado")
        return user
