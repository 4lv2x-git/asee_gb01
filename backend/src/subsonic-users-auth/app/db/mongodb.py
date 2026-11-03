from beanie import init_beanie
from motor.motor_asyncio import AsyncIOMotorClient

from app.core.config import Settings
from app.models import RevokedToken, User, VerificationCode

client: AsyncIOMotorClient | None = None


async def init_db(settings: Settings) -> None:
    global client
    client = AsyncIOMotorClient(settings.mongodb_uri)
    await init_beanie(
        database=client[settings.mongodb_db],
        document_models=[User, RevokedToken, VerificationCode],
    )


async def close_db() -> None:
    if client:
        client.close()
