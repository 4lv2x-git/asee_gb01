from app.api.deps import get_spaces_client
from app.clients.spaces_client import FakeSpacesClient
from app.core.security import hash_password
from app.models.enums import Estado, Rol
from app.repositories.user_repository import UserRepository


async def make_user(correo: str, rol: Rol):
    return await UserRepository().create(
        nombre=rol.value,
        correo=correo,
        password_hash=hash_password("password123"),
        rol=rol,
        email_verificado=True,
    )


async def token_for(client, correo):
    response = await client.post("/v1/auth/login", json={"correo": correo, "password": "password123"})
    return response.json()["accessToken"]


async def test_perfil_get_patch(client):
    await make_user("cliente@example.com", Rol.CLIENTE)
    token = await token_for(client, "cliente@example.com")
    headers = {"Authorization": f"Bearer {token}"}
    assert (await client.get("/v1/usuarios/perfil", headers=headers)).status_code == 200
    response = await client.patch("/v1/usuarios/perfil", headers=headers, json={"nombre": "Nuevo"})
    assert response.status_code == 200
    assert response.json()["nombre"] == "Nuevo"


async def test_admin_get_lista(client):
    await make_user("cliente@example.com", Rol.CLIENTE)
    await make_user("admin@example.com", Rol.ADMIN)
    cliente_token = await token_for(client, "cliente@example.com")
    admin_token = await token_for(client, "admin@example.com")
    assert (await client.get("/v1/usuarios/admin", headers={"Authorization": f"Bearer {cliente_token}"})).status_code == 403
    assert (await client.get("/v1/usuarios/admin", headers={"Authorization": f"Bearer {admin_token}"})).status_code == 200


async def test_admin_suspende_invalida_token(client):
    user = await make_user("cliente@example.com", Rol.CLIENTE)
    await make_user("admin@example.com", Rol.ADMIN)
    user_token = await token_for(client, "cliente@example.com")
    admin_token = await token_for(client, "admin@example.com")
    response = await client.patch(
        f"/v1/usuarios/admin/{user.id}/estado",
        headers={"Authorization": f"Bearer {admin_token}"},
        json={"estado": "suspendido"},
    )
    assert response.status_code == 200
    assert (await client.get("/v1/auth/verify", headers={"Authorization": f"Bearer {user_token}"})).status_code == 403


async def test_admin_password_reset(client):
    user = await make_user("cliente@example.com", Rol.CLIENTE)
    await make_user("admin@example.com", Rol.ADMIN)
    admin_token = await token_for(client, "admin@example.com")
    response = await client.put(
        f"/v1/usuarios/admin-password-reset/{user.id}",
        headers={"Authorization": f"Bearer {admin_token}"},
        json={"nuevaPassword": "newpass123"},
    )
    assert response.status_code == 200
    assert (await client.post("/v1/auth/login", json={"correo": "cliente@example.com", "password": "newpass123"})).status_code == 200


async def test_verify_rol_interno_sin_key(client):
    user = await make_user("cliente@example.com", Rol.CLIENTE)
    response = await client.get(f"/v1/usuarios/{user.id}/verificar-rol?rolEsperado=cliente")
    assert response.status_code == 403


async def test_historial_espacios_usa_cliente(client, app):
    proveedor = await make_user("proveedor@example.com", Rol.PROVEEDOR)
    fake = FakeSpacesClient()
    app.dependency_overrides[get_spaces_client] = lambda: fake
    token = await token_for(client, "proveedor@example.com")
    response = await client.get("/v1/usuarios/historial-espacios", headers={"Authorization": f"Bearer {token}"})
    assert response.status_code == 200
    assert fake.called_with == str(proveedor.id)
