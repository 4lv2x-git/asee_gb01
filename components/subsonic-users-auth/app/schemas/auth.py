from pydantic import EmailStr, Field

from app.schemas.common import CamelModel


class LoginRequest(CamelModel):
    correo: EmailStr
    password: str


class TokenResponse(CamelModel):
    access_token: str
    token_type: str = "bearer"
    expires_in: int


class EmailRequest(CamelModel):
    correo: EmailStr


class VerifyEmailRequest(CamelModel):
    codigo: str = Field(min_length=1)

class ResetPasswordRequest(CamelModel):
    nuevaPassword: str = Field(min_length=8)
    codigo: str = Field(min_length=16)