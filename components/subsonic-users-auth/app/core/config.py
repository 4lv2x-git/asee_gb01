from functools import lru_cache

from pydantic import Field, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "subsonic-users-auth"
    app_env: str = "local"
    api_prefix: str = "/v1"

    mongodb_uri: str = "mongodb://localhost:27017"
    mongodb_db: str = "subsonic_users"

    jwt_secret: str = "cambia-esto"
    jwt_algorithm: str = "HS256"
    access_token_expire_minutes: int = 60

    internal_api_key: str = "cambia-esto-tambien"
    spaces_service_url: str = "http://subsonic-spaces:8000/v1"
    spaces_timeout_seconds: float = 5.0

    admin_email: str = "admin@subsonicfestival.com"
    admin_password: str = "cambia-esto-en-produccion"

    smtp_host: str | None = None
    smtp_port: int | None = None
    smtp_user: str | None = None
    smtp_password: str | None = None

    cors_origins: list[str] = Field(default_factory=lambda: ["http://localhost:3000", "http://localhost:5173"])

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )

    @field_validator("smtp_port", mode="before")
    @classmethod
    def blank_port_to_none(cls, value):
        if isinstance(value, str) and not value.strip():
            return None
        return value

    @field_validator("jwt_secret", "internal_api_key")
    @classmethod
    def validate_secret(cls, value: str, info):
        if not value or not value.strip():
            raise ValueError(f"{info.field_name} no puede estar vacio")
        return value

    def validate_production(self) -> None:
        if self.app_env == "production":
            weak_values = {"cambia-esto", "cambia-esto-tambien"}
            if self.jwt_secret in weak_values or self.internal_api_key in weak_values:
                raise ValueError("JWT_SECRET e INTERNAL_API_KEY deben cambiarse en produccion")


@lru_cache
def get_settings() -> Settings:
    settings = Settings()
    settings.validate_production()
    return settings
