from datetime import datetime, timedelta, timezone

from pymongo.errors import DuplicateKeyError

from app.core.config import get_settings
from app.core.exceptions import ConflictError, ForbiddenError, UnauthorizedError, ValidationError
from app.core.security import (
    create_access_token,
    generate_verification_code,
    hash_code,
    hash_password,
    utc_now,
    verify_password,
)
from app.models.enums import Estado, Rol, TipoCodigo
from app.models.user import User
from app.repositories.revoked_token_repository import RevokedTokenRepository
from app.repositories.user_repository import UserRepository
from app.repositories.verification_code_repository import VerificationCodeRepository
from app.schemas.auth import LoginRequest, TokenResponse
from app.schemas.user import UsuarioRegistro


class AuthService:
    def __init__(
        self,
        users: UserRepository | None = None,
        revoked_tokens: RevokedTokenRepository | None = None,
        verification_codes: VerificationCodeRepository | None = None,
    ):
        self.users = users or UserRepository()
        self.revoked_tokens = revoked_tokens or RevokedTokenRepository()
        self.verification_codes = verification_codes or VerificationCodeRepository()

    async def register(self, data: UsuarioRegistro) -> User:
        if data.rol not in {Rol.CLIENTE, Rol.PROVEEDOR}:
            raise ValidationError("Solo se permite registrar cliente o proveedor")
        if data.rol == Rol.PROVEEDOR and not data.tipo_servicio:
            raise ValidationError("tipoServicio es obligatorio para proveedores")
        try:
            user = await self.users.create(
                nombre=data.nombre,
                correo=str(data.correo),
                password_hash=hash_password(data.password),
                rol=data.rol,
                tipo_servicio=data.tipo_servicio,
                descripcion_servicio=data.descripcion_servicio,
            )
        except DuplicateKeyError as exc:
            raise ConflictError("Correo ya registrado") from exc
        await self.create_verification_code(str(user.id), TipoCodigo.EMAIL_VERIFICATION)
        return user

    async def login(self, data: LoginRequest) -> TokenResponse:
        user = await self.users.get_by_email(str(data.correo))
        if not user or not verify_password(data.password, user.password_hash):
            raise UnauthorizedError("Credenciales invalidas")
        if user.estado == Estado.SUSPENDIDO:
            raise ForbiddenError("Usuario suspendido")
        token, _payload = create_access_token(str(user.id), user.rol.value, user.token_version)
        return TokenResponse(access_token=token, expires_in=get_settings().access_token_expire_minutes * 60)

    async def logout(self, payload: dict) -> None:
        exp = payload.get("exp")
        jti = payload.get("jti")
        user_id = payload.get("sub")
        if not exp or not jti or not user_id:
            raise UnauthorizedError("Token invalido")
        expires_at = datetime.fromtimestamp(int(exp), tz=timezone.utc)
        try:
            await self.revoked_tokens.create(jti=jti, user_id=user_id, expires_at=expires_at)
        except DuplicateKeyError:
            pass

    async def forgot_password(self, correo: str) -> None:
        user = await self.users.get_by_email(correo)
        if not user:
            return
        await self.create_verification_code(str(user.id), TipoCodigo.PASSWORD_RESET)

    async def reset_password(self, codigo: str, nueva_password: str) -> None:
        if not codigo or not nueva_password:
            raise ValidationError("Codigo y nueva contraseña son requeridos")

        code = await self.verification_codes.find_active_by_hash(
            tipo=TipoCodigo.PASSWORD_RESET,
            code_hash=hash_code(codigo),
            now=utc_now(),
        )
        if not code:
            raise ValidationError("Codigo invalido o expirado")
        user = await self.users.get_by_id(code.user_id)
        if len(nueva_password) < 8:
            raise ValidationError("La nueva contraseña debe tener al menos 8 caracteres")
        if not user:
            raise ValidationError("Codigo invalido o expirado")
        await self.users.update(user, {"password_hash": hash_password(nueva_password)})
        await self.verification_codes.mark_used(code)

        # ! Queda pendiente revocar los tokens vigentes del usuario

    async def verify_email(self, codigo: str) -> User:
        code = await self.verification_codes.find_active_by_hash(
            tipo=TipoCodigo.EMAIL_VERIFICATION,
            code_hash=hash_code(codigo),
            now=utc_now(),
        )
        if not code:
            raise ValidationError("Codigo invalido o expirado")
        user = await self.users.get_by_id(code.user_id)
        if not user:
            raise ValidationError("Codigo invalido o expirado")
        await self.verification_codes.mark_used(code)
        return await self.users.update(user, {"email_verificado": True})

    async def create_verification_code(self, user_id: str, tipo: TipoCodigo) -> str:
        await self.verification_codes.invalidate_previous(user_id, tipo)
        code = generate_verification_code()
        await self.verification_codes.create(
            user_id=user_id,
            tipo=tipo,
            code_hash=hash_code(code),
            expires_at=utc_now() + timedelta(minutes=15),
        )
        return code
