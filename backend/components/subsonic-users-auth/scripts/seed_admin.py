import asyncio

from app.core.config import get_settings
from app.db.indexes import ensure_indexes
from app.db.mongodb import close_db, init_db
from app.db.seed import seed_admin


async def main() -> None:
    settings = get_settings()
    await init_db(settings)
    await ensure_indexes()
    await seed_admin(settings)
    await close_db()


if __name__ == "__main__":
    asyncio.run(main())
