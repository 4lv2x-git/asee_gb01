from datetime import datetime

from app.models.enums import TipoCodigo
from app.models.verification_code import VerificationCode


class VerificationCodeRepository:
    async def invalidate_previous(self, user_id: str, tipo: TipoCodigo) -> None:
        await VerificationCode.find(
            VerificationCode.user_id == user_id,
            VerificationCode.tipo == tipo,
            VerificationCode.used == False,  # noqa: E712
        ).update({"$set": {"used": True}})

    async def create(self, *, user_id: str, tipo: TipoCodigo, code_hash: str, expires_at: datetime) -> VerificationCode:
        code = VerificationCode(user_id=user_id, tipo=tipo, code_hash=code_hash, expires_at=expires_at)
        return await code.insert()

    async def find_active_by_hash(self, *, tipo: TipoCodigo, code_hash: str, now: datetime) -> VerificationCode | None:
        return await VerificationCode.find_one(
            VerificationCode.tipo == tipo,
            VerificationCode.code_hash == code_hash,
            VerificationCode.used == False,  # noqa: E712
            VerificationCode.expires_at > now,
        )

    async def mark_used(self, code: VerificationCode) -> VerificationCode:
        code.used = True
        await code.save()
        return code
