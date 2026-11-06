from pymongo import ASCENDING

from app.models.revoked_token import RevokedToken
from app.models.user import User
from app.models.verification_code import VerificationCode


async def ensure_indexes() -> None:
    await User.get_motor_collection().create_index("correo_normalizado", unique=True)
    await User.get_motor_collection().create_index("rol")
    await User.get_motor_collection().create_index("estado")

    await RevokedToken.get_motor_collection().create_index("jti", unique=True)
    await RevokedToken.get_motor_collection().create_index("expires_at", expireAfterSeconds=0)

    await VerificationCode.get_motor_collection().create_index("expires_at", expireAfterSeconds=0)
    await VerificationCode.get_motor_collection().create_index([("user_id", ASCENDING), ("tipo", ASCENDING)])
