from pymongo.collection import Collection


DEMO_ARTISTS = (
    {
        "_id": "demo-adam-beyer",
        "id": "demo-adam-beyer",
        "nombre": "Adam Beyer",
        "imagen": "/images/Adam_Beyer.webp",
        "biografia": "La biografía no está incluida en el proyecto del año pasado.",
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [],
    },
    {
        "_id": "demo-amelie-lens",
        "id": "demo-amelie-lens",
        "nombre": "Amelie Lens",
        "imagen": "/images/Amelie_Lens.jpg",
        "biografia": "La biografía no está incluida en el proyecto del año pasado.",
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [],
    },
    {
        "_id": "demo-boris-brejcha",
        "id": "demo-boris-brejcha",
        "nombre": "Boris Brejcha",
        "imagen": "/images/Boris_Brejcha.jpg",
        "biografia": "La biografía no está incluida en el proyecto del año pasado.",
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [],
    },
    {
        "_id": "demo-carl-cox",
        "id": "demo-carl-cox",
        "nombre": "Carl Cox",
        "imagen": "/images/Carl_Cox.jpg",
        "biografia": "La biografía no está incluida en el proyecto del año pasado.",
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [],
    },
    {
        "_id": "demo-charlotte-de-witte",
        "id": "demo-charlotte-de-witte",
        "nombre": "Charlotte de Witte",
        "imagen": "/images/Charlotte_de_Witte.png",
        "biografia": "La biografía no está incluida en el proyecto del año pasado.",
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [],
    },
    {
        "_id": "demo-nina-kraviz",
        "id": "demo-nina-kraviz",
        "nombre": "Nina Kraviz",
        "imagen": "/images/Nina_Kraviz.jpg",
        "biografia": "La biografía no está incluida en el proyecto del año pasado.",
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [],
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


def seed_demo_artists_if_empty(collection: Collection) -> int:
    if collection.count_documents({}) > 0:
        return 0
    return seed_demo_artists(collection)
