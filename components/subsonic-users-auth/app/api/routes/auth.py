from typing import Annotated

from fastapi import APIRouter, Depends, status

from app.api.deps import get_current_user, get_token_payload
from app.models.user import User
from app.schemas.auth import EmailRequest, LoginRequest, TokenResponse, VerifyEmailRequest, ResetPasswordRequest
from app.schemas.common import MessageResponse
from app.schemas.user import Usuario, UsuarioRegistro, user_to_schema
from app.services.auth_service import AuthService

router = APIRouter(prefix="/auth", tags=["Autenticacion y Registro"])


@router.post("/register", response_model=Usuario, status_code=status.HTTP_201_CREATED)
async def register(data: UsuarioRegistro) -> Usuario:
    user = await AuthService().register(data)
    return user_to_schema(user)


@router.post("/login", response_model=TokenResponse)
async def login(data: LoginRequest) -> TokenResponse:
    return await AuthService().login(data)


@router.post("/logout", response_model=MessageResponse)
async def logout(payload: Annotated[dict, Depends(get_token_payload)]) -> MessageResponse:
    await AuthService().logout(payload)
    return MessageResponse(message="Sesion cerrada correctamente")


@router.get("/verify", response_model=MessageResponse)
async def verify(_user: Annotated[User, Depends(get_current_user)]) -> MessageResponse:
    return MessageResponse(message="Token valido")


@router.post("/forgot-password", response_model=MessageResponse)
async def forgot_password(data: EmailRequest) -> MessageResponse:
    await AuthService().forgot_password(str(data.correo))
    return MessageResponse(message="Si el correo existe, se enviara un enlace de recuperacion")

@router.post("/reset-password", response_model=MessageResponse)
async def reset_password(data: ResetPasswordRequest) -> MessageResponse:
    await AuthService().reset_password(str(data.codigo), str(data.nuevaPassword))
    return MessageResponse(message="Contraseña cambiada correctamente")


@router.post("/verify-email", response_model=Usuario)
async def verify_email(data: VerifyEmailRequest) -> Usuario:
    user = await AuthService().verify_email(data.codigo)
    return user_to_schema(user)
