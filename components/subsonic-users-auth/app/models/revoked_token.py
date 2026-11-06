from datetime import datetime, timezone

from beanie import Document
from pydantic import Field


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


class RevokedToken(Document):
    jti: str
    user_id: str
    expires_at: datetime
    revoked_at: datetime = Field(default_factory=utc_now)

    class Settings:
        name = "revoked_tokens"
