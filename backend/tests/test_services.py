import unittest
from datetime import datetime, timezone

from src.schemas import ArtistRegistration
from src.services import ArtistService, BlogService


class FakeArtistRepository:
    def list(self):
        return [{"id": "artist-1", "nombre": "Artist", "imagen": None}]

    def get(self, artist_id):
        if artist_id != "artist-1":
            return None
        return {
            "id": artist_id,
            "nombre": "Artist",
            "biografia": "Biography",
            "cancionesDestacadas": [{"titulo": "Song"}],
        }

    def get_many(self, artist_ids):
        artists = [self.get(artist_id) for artist_id in artist_ids]
        return [artist for artist in artists if artist is not None]

    def create(self, artist):
        return {
            **artist.model_dump(by_alias=True, exclude_none=True),
            "id": "artist-2",
        }

    def update(self, artist_id, artist):
        return None

    def delete(self, artist_id):
        return artist_id == "artist-1"


class FakeArticleRepository:
    def list(self, skip, limit):
        return [
            {
                "id": "article-1",
                "titulo": "News",
                "resumen": "Summary",
                "autor": "Editor",
                "fechaPublicacion": datetime(2026, 1, 1, tzinfo=timezone.utc),
            }
        ]

    def get(self, article_id):
        if article_id != "article-1":
            return None
        return {
            "id": article_id,
            "titulo": "News",
            "resumen": "Summary",
            "contenido": "Full article",
            "autor": "Editor",
            "fechaPublicacion": datetime(2026, 1, 1, tzinfo=timezone.utc),
        }


class ArtistsServiceTests(unittest.TestCase):
    def setUp(self):
        self.service = ArtistService(FakeArtistRepository())

    def test_artist_detail_and_songs(self):
        artist = self.service.get_artist("artist-1")

        self.assertEqual(artist.nombre, "Artist")
        self.assertEqual(self.service.get_songs("artist-1")[0].titulo, "Song")

    def test_missing_artist_is_not_found(self):
        self.assertIsNone(self.service.get_artist("missing"))
        self.assertIsNone(self.service.get_songs("missing"))

    def test_registration_is_serialized_with_api_field_names(self):
        artist = self.service.create_artist(
            ArtistRegistration(nombre="New", biografia="Biography")
        )

        self.assertEqual(artist.id, "artist-2")
        self.assertEqual(artist.eventos_vinculados, [])


class BlogServiceTests(unittest.TestCase):
    def setUp(self):
        self.service = BlogService(FakeArticleRepository())

    def test_article_list_and_detail(self):
        articles = self.service.list_articles(skip=0, limit=10)
        article = self.service.get_article("article-1")

        self.assertEqual(articles[0].titulo, "News")
        self.assertEqual(article.contenido, "Full article")
        self.assertIsNone(self.service.get_article("missing"))


if __name__ == "__main__":
    unittest.main()
