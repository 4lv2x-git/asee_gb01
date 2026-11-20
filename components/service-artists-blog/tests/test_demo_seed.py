import unittest

from src.schemas import ArtistDetail
from src.seed_demo_artists import DEMO_ARTISTS, seed_demo_artists


class FakeCollection:
    def __init__(self):
        self.replacements = []

    def replace_one(self, query, replacement, upsert):
        self.replacements.append((query, replacement, upsert))


class DemoArtistSeedTests(unittest.TestCase):
    def test_seed_upserts_only_demo_records(self):
        collection = FakeCollection()

        count = seed_demo_artists(collection)

        self.assertEqual(count, 6)
        self.assertEqual(len(collection.replacements), 6)
        for query, replacement, upsert in collection.replacements:
            self.assertEqual(query, {"_id": replacement["_id"]})
            self.assertTrue(replacement["_id"].startswith("demo-"))
            self.assertTrue(upsert)

    def test_demo_bios_and_songs_are_clearly_marked(self):
        for artist in DEMO_ARTISTS:
            self.assertIn("demostración", artist["biografia"].lower())
            self.assertEqual(artist["eventosVinculados"], [])
            self.assertEqual(artist["redesSociales"], {})
            self.assertTrue(
                all(
                    song["titulo"].startswith("[DEMO]")
                    and song["album"].startswith("[DEMO]")
                    for song in artist["cancionesDestacadas"]
                )
            )

    def test_seed_records_match_the_public_artist_contract(self):
        for artist_data in DEMO_ARTISTS:
            artist = ArtistDetail.model_validate(artist_data)

            self.assertEqual(artist.id, artist_data["id"])
            self.assertEqual(artist.nombre, artist_data["nombre"])
            self.assertTrue(artist.canciones_destacadas)


if __name__ == "__main__":
    unittest.main()
