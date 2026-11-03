from src.repositories import ArtistRepository, ArticleRepository
from src.schemas import (
    ArticleDetail,
    ArticleSummary,
    ArtistDetail,
    ArtistRegistration,
    ArtistSummary,
    Song,
)


class ArtistService:
    def __init__(self, repository: ArtistRepository) -> None:
        self._repository = repository

    def list_artists(self) -> list[ArtistSummary]:
        return [
            ArtistSummary.model_validate(artist)
            for artist in self._repository.list()
        ]

    def get_artist(self, artist_id: str) -> ArtistDetail | None:
        artist = self._repository.get(artist_id)
        return ArtistDetail.model_validate(artist) if artist else None

    def get_songs(self, artist_id: str) -> list[Song] | None:
        artist = self._repository.get(artist_id)
        if artist is None:
            return None
        return [
            Song.model_validate(song)
            for song in artist.get("cancionesDestacadas", [])
        ]

    def get_artists(self, artist_ids: list[str]) -> list[ArtistDetail]:
        return [
            ArtistDetail.model_validate(artist)
            for artist in self._repository.get_many(artist_ids)
        ]

    def create_artist(self, artist: ArtistRegistration) -> ArtistDetail:
        return ArtistDetail.model_validate(self._repository.create(artist))

    def update_artist(
        self, artist_id: str, artist: ArtistRegistration
    ) -> ArtistDetail | None:
        updated = self._repository.update(artist_id, artist)
        return ArtistDetail.model_validate(updated) if updated else None

    def delete_artist(self, artist_id: str) -> bool:
        return self._repository.delete(artist_id)


class BlogService:
    def __init__(self, repository: ArticleRepository) -> None:
        self._repository = repository

    def list_articles(self, skip: int, limit: int) -> list[ArticleSummary]:
        return [
            ArticleSummary.model_validate(article)
            for article in self._repository.list(skip, limit)
        ]

    def get_article(self, article_id: str) -> ArticleDetail | None:
        article = self._repository.get(article_id)
        return ArticleDetail.model_validate(article) if article else None
