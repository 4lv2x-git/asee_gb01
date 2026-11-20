import json
import urllib.request

BASE_URL = "http://localhost:8000"


def request(method, path, payload=None):
    data = None if payload is None else json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        BASE_URL + path,
        data=data,
        method=method,
        headers={"Content-Type": "application/json"},
    )
    with urllib.request.urlopen(req) as response:
        if response.status == 204:
            return None
        return json.load(response)


FESTIVALS = [
    {
        "nombre": "Riverland Fest",
        "descripcion": "Festival de música electrónica junto al mar en Asturias, con dos escenarios y una programación internacional.",
        "fechaInicio": "2027-07-16T16:00:00",
        "fechaFin": "2027-07-18T23:30:00",
        "ubicacion": {
            "nombre": "Recinto Riverland",
            "direccion": "Autopista AS-19",
            "ciudad": "Asturias",
            "pais": "España",
            "latitud": 43.3946903,
            "longitud": -5.1770538,
        },
        "imagenUrl": "/images/riverland.png",
        "estado": "publicado",
        "aforo": 18000,
    },
    {
        "nombre": "Boombastic Costa del Sol",
        "descripcion": "Electrónica y grandes headliners frente al Mediterráneo en una edición pensada para vivir el festival hasta el amanecer.",
        "fechaInicio": "2027-08-07T17:00:00",
        "fechaFin": "2027-08-08T23:30:00",
        "ubicacion": {
            "nombre": "Auditorio Municipal",
            "direccion": "Costa del Sol",
            "ciudad": "Torremolinos",
            "pais": "España",
            "latitud": 36.6906704,
            "longitud": -4.4875348,
        },
        "imagenUrl": "/images/boombastic.jpg",
        "estado": "publicado",
        "aforo": 22000,
    },
    {
        "nombre": "Medusa-Zevra Festival",
        "descripcion": "Gran festival urbano junto al mar de Valencia con artistas internacionales, dos escenarios y programación nocturna.",
        "fechaInicio": "2027-08-06T16:00:00",
        "fechaFin": "2027-08-09T05:00:00",
        "ubicacion": {
            "nombre": "Recinto Medusa",
            "direccion": "Cullera",
            "ciudad": "Valencia",
            "pais": "España",
            "latitud": 39.1547556,
            "longitud": -0.2437535,
        },
        "imagenUrl": "/images/zevra.jpg",
        "estado": "publicado",
        "aforo": 30000,
    },
]


PASSES = [
    {
        "nombre": "Abono General",
        "descripcion": "Acceso completo a los escenarios y las zonas públicas del festival.",
        "tipo": "abono",
        "precio": 89,
        "moneda": "EUR",
        "stock": 500,
        "stockReservado": 0,
        "serviciosIncluidos": ["Acceso a escenarios", "Zona food & craft", "App oficial"],
        "estado": "disponible",
    },
    {
        "nombre": "Pase VIP",
        "descripcion": "Acceso prioritario y experiencias exclusivas.",
        "tipo": "vip",
        "precio": 149,
        "moneda": "EUR",
        "stock": 150,
        "stockReservado": 0,
        "serviciosIncluidos": ["Acceso prioritario", "Zona VIP", "Barra exclusiva"],
        "estado": "disponible",
    },
]


ARTISTS = [
    ("charlotte-de-witte", "Charlotte de Witte", "/images/Charlotte_de_Witte.png"),
    ("amelie-lens", "Amelie Lens", "/images/Amelie_Lens.jpg"),
    ("boris-brejcha", "Boris Brejcha", "/images/Boris_Brejcha.jpg"),
    ("adam-beyer", "Adam Beyer", "/images/Adam_Beyer.webp"),
    ("carl-cox", "Carl Cox", "/images/Carl_Cox.jpg"),
    ("nina-kraviz", "Nina Kraviz", "/images/Nina_Kraviz.jpg"),
]


existing = request("GET", "/eventos?limit=100")
if existing["paginacion"]["total"] == 0:
    for index, festival in enumerate(FESTIVALS):
        created = request("POST", "/eventos", festival)
        event_id = created["id"]
        for pase in PASSES:
            request("POST", f"/eventos/{event_id}/pases", pase)
        for artist_index, (artist_id, artist_name, image_url) in enumerate(ARTISTS):
            if artist_index % 3 == index or artist_index < 2:
                request(
                    "POST",
                    f"/eventos/{event_id}/escaleta",
                    {
                        "artistaId": artist_id,
                        "nombreArtista": artist_name,
                        "imagenUrl": image_url,
                        "fecha": festival["fechaInicio"][:10],
                        "horaInicio": f"{20 + (artist_index % 3)}:00",
                        "horaFin": f"{21 + (artist_index % 3)}:30",
                        "escenario": "Escenario Principal" if artist_index % 2 == 0 else "Escenario Subsonic",
                        "orden": artist_index,
                    },
                )
    print("Festivales, pases y escaletas insertados.")
else:
    print("Ya existen festivales en la base de datos.")

for festival in request("GET", "/eventos?limit=100")["data"]:
    event_id = festival["id"]
    aforo = festival["aforo"] or 18000
    pases = request("GET", f"/eventos/{event_id}/pases")["data"]

    for pase in pases:
        tipo = (pase.get("tipo") or "").lower()
        stock = aforo if tipo != "vip" else round(aforo * 0.08)
        request(
            "PUT",
            f"/eventos/{event_id}/pases/{pase['id']}",
            {
                "nombre": pase["nombre"],
                "descripcion": pase.get("descripcion"),
                "tipo": pase.get("tipo"),
                "precio": pase["precio"],
                "moneda": "EUR",
                "stock": stock,
                "serviciosIncluidos": pase.get("serviciosIncluidos", []),
                "estado": "disponible",
            },
        )

    if not any((pase.get("tipo") or "").lower() == "camping" for pase in pases):
        request(
            "POST",
            f"/eventos/{event_id}/pases",
            {
                "nombre": "Camping",
                "descripcion": "Acceso a zona de acampada durante el festival",
                "tipo": "camping",
                "precio": 45,
                "moneda": "EUR",
                "stock": 1000,
                "stockReservado": 0,
                "serviciosIncluidos": ["Parcela", "Duchas", "Zona de sombra"],
                "estado": "disponible",
            },
        )
