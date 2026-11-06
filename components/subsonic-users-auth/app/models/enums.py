from enum import Enum


class Rol(str, Enum):
    CLIENTE = "cliente"
    PROVEEDOR = "proveedor"
    ADMIN = "admin"
    VISITANTE = "visitante"


class Estado(str, Enum):
    ACTIVO = "activo"
    SUSPENDIDO = "suspendido"


class TipoCodigo(str, Enum):
    EMAIL_VERIFICATION = "email_verification"
    PASSWORD_RESET = "password_reset"
