from jose import jwt

from app.core.config import get_settings
from app.models.enums import Estado, Rol, TipoCodigo
from app.models.user import User
from app.models.verification_code import VerificationCode


async def register(client, correo="cliente@example.com", rol="cliente", **extra):
    payload = {
        "nombre": "Cliente",
        "correo": correo,
        "password": "password123",
        "rol": rol,
        **extra,
    }
    return await client.post("/v1/auth/register", json=payload)


async def login(client, correo="cliente@example.com", password="password123"):
    return await client.post("/v1/auth/login", json={"correo": correo, "password": password})


async def test_register_cliente(client):
    response = await register(client)
    assert response.status_code == 201
    data = response.json()
    assert data["correo"] == "cliente@example.com"
    assert "password" not in data
    assert "passwordHash" not in data


async def test_register_proveedor_sin_tipo(client):
    response = await register(client, rol="proveedor")
    assert response.status_code == 422


async def test_register_admin_rechazado(client):
    response = await register(client, rol="admin")
    assert response.status_code == 422


async def test_login_ok(client):
    await register(client)
    response = await login(client)
    assert response.status_code == 200
    token = response.json()["accessToken"]
    payload = jwt.decode(token, get_settings().jwt_secret, algorithms=[get_settings().jwt_algorithm])
    assert payload["sub"]
    assert payload["rol"] == "cliente"
    assert payload["jti"]
    assert payload["exp"]


async def test_login_case_insensitive(client):
    await register(client, correo="User@Example.com")
    response = await login(client, correo="USER@example.com")
    assert response.status_code == 200


async def test_login_suspendido(client):
    await register(client)
    user = await User.find_one(User.correo_normalizado == "cliente@example.com")
    user.estado = Estado.SUSPENDIDO
    await user.save()
    response = await login(client)
    assert response.status_code == 403


async def test_logout_revoca_jti(client):
    await register(client)
    token = (await login(client)).json()["accessToken"]
    headers = {"Authorization": f"Bearer {token}"}
    assert (await client.post("/v1/auth/logout", headers=headers)).status_code == 200
    assert (await client.get("/v1/auth/verify", headers=headers)).status_code == 401


async def test_verify_email_marca_usuario(client):
    await register(client)
    code = await VerificationCode.find_one(VerificationCode.tipo == TipoCodigo.EMAIL_VERIFICATION)
    code.code_hash = __import__("hashlib").sha256(b"codigo").hexdigest()
    await code.save()
    response = await client.post("/v1/auth/verify-email", json={"codigo": "codigo"})
    assert response.status_code == 200
    assert response.json()["emailVerificado"] is True


async def test_verify_email_expirado(client):
    response = await client.post("/v1/auth/verify-email", json={"codigo": "nope"})
    assert response.status_code == 400
