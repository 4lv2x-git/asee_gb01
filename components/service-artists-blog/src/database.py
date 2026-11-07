from pymongo import MongoClient
from pymongo.database import Database

from src.config import Settings


def connect(settings: Settings) -> MongoClient:
    return MongoClient(
        settings.mongodb_url,
        serverSelectionTimeoutMS=3000,
        tz_aware=True,
    )


def get_database(client: MongoClient, settings: Settings) -> Database:
    return client[settings.mongodb_database]
