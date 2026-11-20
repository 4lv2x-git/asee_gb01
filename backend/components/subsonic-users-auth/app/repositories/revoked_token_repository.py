from datetime import datetime

from app.models.revoked_token import RevokedToken


class RevokedTokenRepository:
    async def create(self, *, jti: str, user_id: str, expires_at: datetime) -> RevokedToken:
        revoked = RevokedToken(jti=jti, user_id=user_id, expires_at=expires_at)
        return await revoked.insert()

    async def exists(self, jti: str) -> bool:
        return await RevokedToken.find_one(RevokedToken.jti == jti) is not None
