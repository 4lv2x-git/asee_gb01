from datetime import datetime, timezone

from beanie import Document
from pydantic import Field

from app.models.enums import TipoCodigo


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


class VerificationCode(Document):
    user_id: str
    tipo: TipoCodigo
    code_hash: str
    expires_at: datetime
    used: bool = False
    created_at: datetime = Field(default_factory=utc_now)

    class Settings:
        name = "verification_codes"
