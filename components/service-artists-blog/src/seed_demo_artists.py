from pymongo.collection import Collection

from src.config import Settings
from src.database import connect, get_database


DEMO_ARTISTS = (
    {
        "_id": "demo-adam-beyer",
        "id": "demo-adam-beyer",
        "nombre": "Adam Beyer",
        "imagen": "/images/Adam_Beyer.webp",
        "biografia": (
            "Ficha de demostración. Esta biografía no está incluida en el "
            "proyecto anterior y debe sustituirse por información verificada."
        ),
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [
            {
                "titulo": "[DEMO] Canción ficticia, no es un lanzamiento real",
                "album": "[DEMO] Álbum ficticio",
            }
        ],
    },
    {
        "_id": "demo-amelie-lens",
        "id": "demo-amelie-lens",
        "nombre": "Amelie Lens",
        "imagen": "/images/Amelie_Lens.jpg",
        "biografia": (
            "Ficha de demostración. Esta biografía no está incluida en el "
            "proyecto anterior y debe sustituirse por información verificada."
        ),
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [
            {
                "titulo": "[DEMO] Canción ficticia, no es un lanzamiento real",
                "album": "[DEMO] Álbum ficticio",
            }
        ],
    },
    {
        "_id": "demo-boris-brejcha",
        "id": "demo-boris-brejcha",
        "nombre": "Boris Brejcha",
        "imagen": "/images/Boris_Brejcha.jpg",
        "biografia": (
            "Ficha de demostración. Esta biografía no está incluida en el "
            "proyecto anterior y debe sustituirse por información verificada."
        ),
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [
            {
                "titulo": "[DEMO] Canción ficticia, no es un lanzamiento real",
                "album": "[DEMO] Álbum ficticio",
            }
        ],
    },
    {
        "_id": "demo-carl-cox",
        "id": "demo-carl-cox",
        "nombre": "Carl Cox",
        "imagen": "/images/Carl_Cox.jpg",
        "biografia": (
            "Ficha de demostración. Esta biografía no está incluida en el "
            "proyecto anterior y debe sustituirse por información verificada."
        ),
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [
            {
                "titulo": "[DEMO] Canción ficticia, no es un lanzamiento real",
                "album": "[DEMO] Álbum ficticio",
            }
        ],
    },
    {
        "_id": "demo-charlotte-de-witte",
        "id": "demo-charlotte-de-witte",
        "nombre": "Charlotte de Witte",
        "imagen": "/images/Charlotte_de_Witte.png",
        "biografia": (
            "Ficha de demostración. Esta biografía no está incluida en el "
            "proyecto anterior y debe sustituirse por información verificada."
        ),
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [
            {
                "titulo": "[DEMO] Canción ficticia, no es un lanzamiento real",
                "album": "[DEMO] Álbum ficticio",
            }
        ],
    },
    {
        "_id": "demo-nina-kraviz",
        "id": "demo-nina-kraviz",
        "nombre": "Nina Kraviz",
        "imagen": "/images/Nina_Kraviz.jpg",
        "biografia": (
            "Ficha de demostración. Esta biografía no está incluida en el "
            "proyecto anterior y debe sustituirse por información verificada."
        ),
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [
            {
                "titulo": "[DEMO] Canción ficticia, no es un lanzamiento real",
                "album": "[DEMO] Álbum ficticio",
            }
        ],
    },
)


def seed_demo_artists(collection: Collection) -> int:
    for artist in DEMO_ARTISTS:
        collection.replace_one(
            {"_id": artist["_id"]},
            artist,
            upsert=True,
        )
    return len(DEMO_ARTISTS)


def main() -> None:
    settings = Settings.from_environment()
    client = connect(settings)
    try:
        collection = get_database(client, settings)["artistas"]
        count = seed_demo_artists(collection)
    finally:
        client.close()

    print(f"Se han cargado {count} fichas de demostración de artistas.")


if __name__ == "__main__":
    main()
