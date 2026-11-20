from __future__ import annotations

from typing import Any

from bson import ObjectId
from pymongo import ReturnDocument
from pymongo.collection import Collection

from src.schemas import ArtistRegistration


def _with_public_id(document: dict[str, Any]) -> dict[str, Any]:
    result = dict(document)
    identifier = result.pop("_id", None)
    result["id"] = str(result.get("id", identifier))
    return result


def _id_filter(identifier: str) -> dict[str, Any]:
    candidates: list[Any] = [identifier]
    if ObjectId.is_valid(identifier):
        candidates.append(ObjectId(identifier))
    return {"$or": [{"_id": value} for value in candidates] + [{"id": identifier}]}


class ArtistRepository:
    def __init__(self, collection: Collection) -> None:
        self._collection = collection

    def list(self) -> list[dict[str, Any]]:
        return [
            _with_public_id(document)
            for document in self._collection.find(
                {}, {"nombre": 1, "imagen": 1}
            ).sort("nombre", 1)
        ]

    def get(self, artist_id: str) -> dict[str, Any] | None:
        document = self._collection.find_one(_id_filter(artist_id))
        return _with_public_id(document) if document else None

    def get_many(self, artist_ids: list[str]) -> list[dict[str, Any]]:
        if not artist_ids:
            return []
        candidates: list[Any] = list(artist_ids)
        candidates.extend(
            ObjectId(value) for value in artist_ids if ObjectId.is_valid(value)
        )
        query = {
            "$or": [
                {"_id": {"$in": candidates}},
                {"id": {"$in": artist_ids}},
            ]
        }
        return [
            _with_public_id(document)
            for document in self._collection.find(query)
        ]

    def create(self, artist: ArtistRegistration) -> dict[str, Any]:
        document = artist.model_dump(by_alias=True, exclude_none=True)
        result = self._collection.insert_one(document)
        document["id"] = str(result.inserted_id)
        return document

    def update(
        self, artist_id: str, artist: ArtistRegistration
    ) -> dict[str, Any] | None:
        document = artist.model_dump(by_alias=True, exclude_none=True)
        updated = self._collection.find_one_and_update(
            _id_filter(artist_id),
            {"$set": document},
            return_document=ReturnDocument.AFTER,
        )
        return _with_public_id(updated) if updated else None

    def delete(self, artist_id: str) -> bool:
        return self._collection.delete_one(_id_filter(artist_id)).deleted_count > 0


class ArticleRepository:
    def __init__(self, collection: Collection) -> None:
        self._collection = collection

    def list(self, skip: int, limit: int) -> list[dict[str, Any]]:
        documents = (
            self._collection.find(
                {},
                {
                    "titulo": 1,
                    "resumen": 1,
                    "imagen": 1,
                    "autor": 1,
                    "fechaPublicacion": 1,
                },
            )
            .sort("fechaPublicacion", -1)
            .skip(skip)
            .limit(limit)
        )
        return [_with_public_id(document) for document in documents]

    def get(self, article_id: str) -> dict[str, Any] | None:
        document = self._collection.find_one(_id_filter(article_id))
        return _with_public_id(document) if document else None
