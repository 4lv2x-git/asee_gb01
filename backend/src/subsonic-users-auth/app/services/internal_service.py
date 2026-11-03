from app.models.enums import Estado, Rol
from app.repositories.user_repository import UserRepository


class InternalService:
    def __init__(self, users: UserRepository | None = None):
        self.users = users or UserRepository()

    async def verificar_rol(self, user_id: str, rol_esperado: Rol) -> bool:
        user = await self.users.get_by_id(user_id)
        if not user or user.estado == Estado.SUSPENDIDO:
            return False
        return user.rol == rol_esperado
