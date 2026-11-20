from pymongo.collection import Collection


# Texto heredado que ya no debe mostrarse. Se limpia también en bases de datos
# que ya contengan las fichas de demostración.
LEGACY_BIOGRAPHY = "La biografía no está incluida en el proyecto del año pasado."


DEMO_ARTISTS = (
    {
        "_id": "demo-adam-beyer",
        "id": "demo-adam-beyer",
        "nombre": "Adam Beyer",
        "imagen": "/images/Adam_Beyer.webp",
        "biografia": "",
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [],
    },
    {
        "_id": "demo-amelie-lens",
        "id": "demo-amelie-lens",
        "nombre": "Amelie Lens",
        "imagen": "/images/Amelie_Lens.jpg",
        "biografia": "",
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [],
    },
    {
        "_id": "demo-boris-brejcha",
        "id": "demo-boris-brejcha",
        "nombre": "Boris Brejcha",
        "imagen": "/images/Boris_Brejcha.jpg",
        "biografia": "",
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [],
    },
    {
        "_id": "demo-carl-cox",
        "id": "demo-carl-cox",
        "nombre": "Carl Cox",
        "imagen": "/images/Carl_Cox.jpg",
        "biografia": "",
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [],
    },
    {
        "_id": "demo-charlotte-de-witte",
        "id": "demo-charlotte-de-witte",
        "nombre": "Charlotte de Witte",
        "imagen": "/images/Charlotte_de_Witte.png",
        "biografia": "",
        "redesSociales": {},
        "eventosVinculados": [],
        "cancionesDestacadas": [],
    },
    {
        "_id": "demo-nina-kraviz",
        "id": "demo-nina-kraviz",
        "nombre": "Nina Kraviz",
        "imagen": "/images/Nina_Kraviz.jpg",
        "biografia": "",
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


def limpiar_biografias_obsoletas(collection: Collection) -> int:
    """Quita el texto heredado de las fichas demo ya guardadas en MongoDB."""
    ids_demo = [artist["_id"] for artist in DEMO_ARTISTS]
    resultado = collection.update_many(
        {"_id": {"$in": ids_demo}, "biografia": LEGACY_BIOGRAPHY},
        {"$set": {"biografia": ""}},
    )
    return resultado.modified_count


def seed_demo_artists_if_empty(collection: Collection) -> int:
    # La limpieza se ejecuta siempre: el volumen de MongoDB se conserva entre
    # arranques y las fichas antiguas seguirían mostrando el texto eliminado.
    limpiar_biografias_obsoletas(collection)
    if collection.count_documents({}) > 0:
        return 0
    return seed_demo_artists(collection)
