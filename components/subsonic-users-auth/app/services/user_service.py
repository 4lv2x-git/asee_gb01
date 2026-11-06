from app.core.exceptions import NotFoundError
from app.models.user import User
from app.repositories.user_repository import UserRepository
from app.schemas.user import UsuarioEdicion


class UserService:
    def __init__(self, users: UserRepository | None = None):
        self.users = users or UserRepository()

    async def update_profile(self, user: User, data: UsuarioEdicion) -> User:
        update_data = data.model_dump(exclude_unset=True, by_alias=False)
        return await self.users.update(user, update_data)

    async def delete_profile(self, user: User) -> None:
        await self.users.delete(user)

    async def get_existing(self, user_id: str) -> User:
        user = await self.users.get_by_id(user_id)
        if not user:
            raise NotFoundError("Usuario no encontrado")
        return user
