import gc
import os
from pathlib import Path

os.environ["DATABASE_URL"] = "sqlite:///./test-festivals.db"

from fastapi.testclient import TestClient

from src.main import app
from src.database import engine


client = TestClient(app)


def test_event_crud_and_pass_availability():
    created = client.post(
        "/eventos",
        json={
            "nombre": "Prueba Festival",
            "descripcion": "Festival usado por las pruebas",
            "fechaInicio": "2027-07-16T16:00:00",
            "fechaFin": "2027-07-18T23:30:00",
            "ubicacion": {
                "ciudad": "Madrid",
                "pais": "España",
                "latitud": 40.4168,
                "longitud": -3.7038,
            },
            "imagenUrl": "/images/riverland.png",
            "estado": "publicado",
            "aforo": 1000,
        },
    )
    assert created.status_code == 201
    event = created.json()

    listed = client.get("/eventos", params={"nombre": "Prueba", "estado": "publicado"})
    assert listed.status_code == 200
    assert listed.json()["paginacion"]["total"] >= 1

    pase = client.post(
        f"/eventos/{event['id']}/pases",
        json={
            "nombre": "General",
            "precio": 60,
            "stock": 1000,
            "tipo": "abono",
            "estado": "disponible",
            "serviciosIncluidos": ["Escenarios", "Food & craft"],
        },
    )
    assert pase.status_code == 201

    disponibilidad = client.get(
        f"/eventos/{event['id']}/pases/{pase.json()['id']}/disponibilidad",
        params={"cantidad": 2},
    )
    assert disponibilidad.status_code == 200
    assert disponibilidad.json()["disponible"] is True


def teardown_module():
    engine.dispose()
    gc.collect()
    Path("test-festivals.db").unlink(missing_ok=True)
