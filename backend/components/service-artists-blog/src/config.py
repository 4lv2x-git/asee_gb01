import os
from dataclasses import dataclass


@dataclass(frozen=True)
class Settings:
    mongodb_url: str
    mongodb_database: str
    jwt_secret: str | None
    jwt_algorithm: str

    @classmethod
    def from_environment(cls) -> "Settings":
        return cls(
            mongodb_url=os.getenv("MONGODB_URL", "mongodb://localhost:27017"),
            mongodb_database=os.getenv(
                "MONGODB_DATABASE", "subsonic_festival"
            ),
            jwt_secret=os.getenv("JWT_SECRET") or None,
            jwt_algorithm=os.getenv("JWT_ALGORITHM", "HS256"),
        )
