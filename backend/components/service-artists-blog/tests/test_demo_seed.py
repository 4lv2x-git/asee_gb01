import asyncio
import unittest
from unittest.mock import patch

from src.config import Settings
from src.main import create_app
from src.schemas import ArtistDetail
from src.seed_demo_artists import (
    DEMO_ARTISTS,
    LEGACY_BIOGRAPHY,
    limpiar_biografias_obsoletas,
    seed_demo_artists,
    seed_demo_artists_if_empty,
)


class FakeResult:
    def __init__(self, modified_count=0):
        self.modified_count = modified_count


class FakeCollection:
    def __init__(self, existing_count=0, legacy_biographies=0):
        self.existing_count = existing_count
        self.replacements = []
        self.updates = []
        self.legacy_biographies = legacy_biographies

    def count_documents(self, query):
        return self.existing_count

    def replace_one(self, query, replacement, upsert):
        self.replacements.append((query, replacement, upsert))

    def update_many(self, query, update):
        self.updates.append((query, update))
        return FakeResult(self.legacy_biographies)


class FakeClient:
    def __init__(self):
        self.closed = False

    def close(self):
        self.closed = True


class DemoArtistSeedTests(unittest.TestCase):
    def test_seed_contains_the_six_demo_artists(self):
        self.assertEqual(
            {artist["nombre"] for artist in DEMO_ARTISTS},
            {
                "Adam Beyer",
                "Amelie Lens",
                "Boris Brejcha",
                "Carl Cox",
                "Charlotte de Witte",
                "Nina Kraviz",
            },
        )

    def test_seed_upserts_only_demo_records(self):
        collection = FakeCollection()

        count = seed_demo_artists(collection)

        self.assertEqual(count, 6)
        self.assertEqual(len(collection.replacements), 6)
        for query, replacement, upsert in collection.replacements:
            self.assertEqual(query, {"_id": replacement["_id"]})
            self.assertTrue(replacement["_id"].startswith("demo-"))
            self.assertTrue(upsert)

    def test_seed_runs_only_for_an_empty_artist_collection(self):
        empty_collection = FakeCollection()
        populated_collection = FakeCollection(existing_count=1)

        self.assertEqual(seed_demo_artists_if_empty(empty_collection), 6)
        self.assertEqual(seed_demo_artists_if_empty(populated_collection), 0)
        self.assertEqual(len(populated_collection.replacements), 0)

    def test_backend_lifespan_seeds_using_its_configured_database(self):
        settings = Settings(
            mongodb_url="mongodb://team-database:27017",
            mongodb_database="subsonic_test",
            jwt_secret=None,
            jwt_algorithm="HS256",
        )
        client = FakeClient()
        collection = FakeCollection()
        app = create_app(settings)

        async def run_lifespan():
            async with app.router.lifespan_context(app):
                pass

        with (
            patch("src.main.connect", return_value=client) as connect,
            patch("src.main.get_database", return_value={"artistas": collection}),
        ):
            asyncio.run(run_lifespan())

        connect.assert_called_once_with(settings)
        self.assertEqual(len(collection.replacements), 6)
        self.assertTrue(client.closed)

    def test_missing_artist_details_are_not_fabricated(self):
        for artist in DEMO_ARTISTS:
            self.assertEqual(artist["biografia"], "")
            self.assertEqual(artist["eventosVinculados"], [])
            self.assertEqual(artist["redesSociales"], {})
            self.assertEqual(artist["cancionesDestacadas"], [])

    def test_legacy_biographies_are_cleared_on_every_startup(self):
        collection = FakeCollection(existing_count=6, legacy_biographies=6)

        self.assertEqual(limpiar_biografias_obsoletas(collection), 6)
        self.assertEqual(seed_demo_artists_if_empty(collection), 0)
        self.assertEqual(len(collection.updates), 2)
        for query, update in collection.updates:
            self.assertEqual(len(query["_id"]["$in"]), 6)
            self.assertEqual(query["biografia"], LEGACY_BIOGRAPHY)
            self.assertEqual(update, {"$set": {"biografia": ""}})

    def test_seed_records_match_the_public_artist_contract(self):
        for artist_data in DEMO_ARTISTS:
            artist = ArtistDetail.model_validate(artist_data)

            self.assertEqual(artist.id, artist_data["id"])
            self.assertEqual(artist.nombre, artist_data["nombre"])
            self.assertEqual(artist.canciones_destacadas, [])


if __name__ == "__main__":
    unittest.main()
