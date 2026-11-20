import gc
import os
from pathlib import Path

os.environ["DATABASE_URL"] = "sqlite:///./test-grounds.db"

from fastapi.testclient import TestClient

from app.main import app
from app.database import engine


client = TestClient(app)


def test_ground_create_list_detail_update_delete():
    created = client.post(
        "/api/grounds",
        json={
            "evento_id": "1",
            "tipo": "Comida",
            "dimensiones": "4 x 3 m",
            "ubicacion": "Zona norte",
            "servicios_incluidos": ["Electricidad", "Agua"],
            "precio": 450,
            "estado": "disponible",
        },
        headers={"X-User-Role": "admin"},
    )
    assert created.status_code == 201
    ground = created.json()

    listed = client.get("/api/grounds", params={"tipo": "Comida"})
    assert listed.status_code == 200
    assert any(item["id"] == ground["id"] for item in listed.json())

    detail = client.get(f"/api/grounds/{ground['id']}")
    assert detail.status_code == 200
    assert detail.json()["tipo"] == "Comida"

    rented = client.post(
        f"/api/grounds/{ground['id']}/alquilar",
        json={"proveedor_id": "provider-1"},
        headers={"X-User-Role": "proveedor"},
    )
    assert rented.status_code == 200
    assert rented.json()["estado"] == "reservado"

    deleted = client.delete(
        f"/api/grounds/{ground['id']}",
        headers={"X-User-Role": "admin"},
    )
    assert deleted.status_code == 204


def teardown_module():
    engine.dispose()
    gc.collect()
    Path("test-grounds.db").unlink(missing_ok=True)
