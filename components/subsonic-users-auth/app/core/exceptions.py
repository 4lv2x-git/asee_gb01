from typing import Any


class AppError(Exception):
    status_code: int = 500
    code: str = "INTERNAL_ERROR"
    message: str = "Error interno"

    def __init__(self, message: str | None = None, details: dict[str, Any] | None = None):
        self.message = message or self.message
        self.details = details or {}


class NotFoundError(AppError):
    status_code = 404
    code = "NOT_FOUND"
    message = "Recurso no encontrado"


class ConflictError(AppError):
    status_code = 409
    code = "CONFLICT"
    message = "Conflicto"


class UnauthorizedError(AppError):
    status_code = 401
    code = "UNAUTHORIZED"
    message = "No autorizado"


class ForbiddenError(AppError):
    status_code = 403
    code = "FORBIDDEN"
    message = "Prohibido"


class ValidationError(AppError):
    status_code = 400
    code = "VALIDATION_ERROR"
    message = "Datos no validos"


class BadGatewayError(AppError):
    status_code = 502
    code = "BAD_GATEWAY"
    message = "Servicio externo no disponible"
