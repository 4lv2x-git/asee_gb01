from app.core.config import Settings
from app.core.security import hash_password
from app.models.enums import Estado, Rol
from app.repositories.user_repository import UserRepository


async def seed_admin(settings: Settings) -> None:
    repo = UserRepository()
    existing = await repo.get_by_email(settings.admin_email)
    if existing:
        return
    await repo.create(
        nombre="Administrador",
        correo=settings.admin_email,
        password_hash=hash_password(settings.admin_password),
        rol=Rol.ADMIN,
        estado=Estado.ACTIVO,
        email_verificado=True,
    )
