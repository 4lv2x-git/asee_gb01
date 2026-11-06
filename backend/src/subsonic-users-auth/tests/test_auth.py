from datetime import timedelta

import pytest
from jose import jwt

from app.core.config import get_settings
from app.core.security import hash_code, utc_now, verify_password
from app.models.enums import Estado, TipoCodigo
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
    registered = await register(client)
    assert registered.status_code == 201
    response = await login(client)
    assert response.status_code == 200
    data = response.json()
    assert set(data) == {"accessToken", "tokenType", "expiresIn"}
    assert data["tokenType"] == "bearer"
    assert type(data["expiresIn"]) is int
    assert data["expiresIn"] == get_settings().access_token_expire_minutes * 60
    token = data["accessToken"]
    payload = jwt.decode(token, get_settings().jwt_secret, algorithms=[get_settings().jwt_algorithm])
    assert payload["sub"] == registered.json()["id"]
    assert payload["rol"] == "cliente"
    assert payload["jti"]
    assert payload["exp"]
    assert payload["exp"] - payload["iat"] == data["expiresIn"]
    assert (await client.get("/v1/auth/verify", headers={"Authorization": f"Bearer {token}"})).status_code == 200


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


@pytest.mark.parametrize("rol,extra", [("cliente", {}), ("proveedor", {"tipoServicio": "sonido"})])
async def test_direccion_se_guarda_y_devuelve_en_perfil(client, rol, extra):
    response = await register(client, rol=rol, **extra)
    assert response.status_code == 201
    assert response.json()["direccion"] is None
    token = (await login(client)).json()["accessToken"]
    headers = {"Authorization": f"Bearer {token}"}
    direccion = "Calle Mayor 10, Madrid"
    response = await client.patch("/v1/usuarios/perfil", headers=headers, json={"direccion": direccion})
    assert response.status_code == 200
    assert response.json()["direccion"] == direccion
    user = await User.find_one(User.correo_normalizado == "cliente@example.com")
    assert user.direccion == direccion
    # Una edicion de otro campo debe conservar la direccion persistida.
    response = await client.patch("/v1/usuarios/perfil", headers=headers, json={"nombre": "Nuevo"})
    assert response.status_code == 200
    response = await client.get("/v1/usuarios/perfil", headers=headers)
    assert response.status_code == 200
    assert response.json()["direccion"] == direccion
    response = await client.patch("/v1/usuarios/perfil", headers=headers, json={"direccion": None})
    assert response.status_code == 200
    assert response.json()["direccion"] is None
    user = await User.get(user.id)
    assert user.direccion is None


@pytest.mark.parametrize("internal_key", [None, "clave-incorrecta", "clave-test-interna"])
async def test_verificar_rol_exige_clave_interna_incluso_con_jwt(client, monkeypatch, internal_key):
    monkeypatch.setattr(get_settings(), "internal_api_key", "clave-test-interna")
    registered = await register(client)
    assert registered.status_code == 201
    user_id = registered.json()["id"]
    token = (await login(client)).json()["accessToken"]
    headers = {"Authorization": f"Bearer {token}"}
    if internal_key is not None:
        headers["X-Internal-Key"] = internal_key
    response = await client.get(
        f"/v1/usuarios/{user_id}/verificar-rol", headers=headers, params={"rolEsperado": "cliente"}
    )
    assert response.status_code == (200 if internal_key == "clave-test-interna" else 403)
    if response.status_code == 200:
        assert response.json() == {"id": user_id, "rolValido": True}


@pytest.mark.parametrize("rol_esperado,valido", [("cliente", True), ("proveedor", False)])
async def test_verificar_rol_con_clave_interna_sin_jwt(client, monkeypatch, rol_esperado, valido):
    monkeypatch.setattr(get_settings(), "internal_api_key", "clave-test-interna")
    registered = await register(client)
    assert registered.status_code == 201
    user_id = registered.json()["id"]
    response = await client.get(
        f"/v1/usuarios/{user_id}/verificar-rol",
        headers={"X-Internal-Key": "clave-test-interna"},
        params={"rolEsperado": rol_esperado},
    )
    assert response.status_code == 200
    assert response.json() == {"id": user_id, "rolValido": valido}


@pytest.fixture
async def password_reset(client, monkeypatch):
    """Genera un codigo conocido mediante la API, sin simular su validacion."""
    registered = await register(client)
    assert registered.status_code == 201
    raw_code = "codigo-recuperacion-prueba-123456"
    monkeypatch.setattr("app.services.auth_service.generate_verification_code", lambda: raw_code)
    response = await client.post("/v1/auth/forgot-password", json={"correo": "cliente@example.com"})
    assert response.status_code == 200
    code = await VerificationCode.find_one(VerificationCode.tipo == TipoCodigo.PASSWORD_RESET)
    assert code is not None
    assert code.user_id == registered.json()["id"]
    return raw_code, code


async def test_forgot_password_guarda_hash_y_caducidad(password_reset):
    raw_code, code = password_reset
    assert code.code_hash == hash_code(raw_code)
    assert code.code_hash != raw_code
    assert code.used is False
    assert timedelta(minutes=14) < code.expires_at - code.created_at <= timedelta(minutes=15)


async def test_forgot_password_no_revela_si_existe_correo(client, password_reset):
    before = await VerificationCode.find_all().count()
    unknown = await client.post("/v1/auth/forgot-password", json={"correo": "desconocido@example.com"})
    assert unknown.status_code == 200
    assert await VerificationCode.find_all().count() == before
    known = await client.post("/v1/auth/forgot-password", json={"correo": "CLIENTE@example.com"})
    assert known.status_code == 200
    assert unknown.json() == known.json()
    assert set(known.json()) == {"message"}
    assert await VerificationCode.find_all().count() == before + 1


async def test_forgot_password_invalida_codigo_anterior(client, monkeypatch, password_reset):
    raw_code, old_code = password_reset
    new_code = "otro-codigo-recuperacion-123456"
    monkeypatch.setattr("app.services.auth_service.generate_verification_code", lambda: new_code)
    response = await client.post("/v1/auth/forgot-password", json={"correo": "cliente@example.com"})
    assert response.status_code == 200
    assert (await VerificationCode.get(old_code.id)).used is True
    active = await VerificationCode.find(
        VerificationCode.tipo == TipoCodigo.PASSWORD_RESET, VerificationCode.used == False
    ).to_list()
    assert len(active) == 1
    assert active[0].code_hash == hash_code(new_code)
    email_code = await VerificationCode.find_one(VerificationCode.tipo == TipoCodigo.EMAIL_VERIFICATION)
    assert email_code.used is False
    response = await client.post(
        "/v1/auth/reset-password", json={"codigo": raw_code, "nuevaPassword": "nuevaPassword123"}
    )
    assert response.status_code == 400


async def test_reset_password_cambia_credenciales(client, password_reset):
    raw_code, code = password_reset
    response = await client.post(
        "/v1/auth/reset-password", json={"codigo": raw_code, "nuevaPassword": "nuevaPassword123"}
    )
    assert response.status_code == 200
    user = await User.find_one(User.correo_normalizado == "cliente@example.com")
    assert verify_password("nuevaPassword123", user.password_hash)
    assert not verify_password("password123", user.password_hash)
    assert (await VerificationCode.get(code.id)).used is True
    assert (await login(client)).status_code == 401
    assert (await login(client, password="nuevaPassword123")).status_code == 200


async def test_reset_password_codigo_de_un_solo_uso(client, password_reset):
    raw_code, code = password_reset
    payload = {"codigo": raw_code, "nuevaPassword": "nuevaPassword123"}
    assert (await client.post("/v1/auth/reset-password", json=payload)).status_code == 200
    assert (await VerificationCode.get(code.id)).used is True
    assert (await client.post("/v1/auth/reset-password", json=payload)).status_code == 400


@pytest.mark.parametrize("caso", ["desconocido", "expirado", "usado", "verificacion_email"])
async def test_reset_password_rechaza_codigos_invalidos(client, password_reset, caso):
    raw_code, code = password_reset
    if caso == "desconocido":
        raw_code = "codigo-inexistente-123456"
    elif caso == "expirado":
        code.expires_at = utc_now() - timedelta(minutes=1)
    elif caso == "usado":
        code.used = True
    else:
        code.tipo = TipoCodigo.EMAIL_VERIFICATION
    await code.save()
    response = await client.post(
        "/v1/auth/reset-password", json={"codigo": raw_code, "nuevaPassword": "nuevaPassword123"}
    )
    assert response.status_code == 400
    assert (await VerificationCode.get(code.id)).used == code.used
    assert (await login(client)).status_code == 200
    assert (await login(client, password="nuevaPassword123")).status_code == 401


@pytest.mark.parametrize("changes", [{"codigo": "corto"}, {"codigo": ""}, {"nuevaPassword": "1234567"}, {"nuevaPassword": ""}])
async def test_reset_password_valida_datos_sin_consumir_codigo(client, password_reset, changes):
    raw_code, code = password_reset
    payload = {"codigo": raw_code, "nuevaPassword": "nuevaPassword123", **changes}
    response = await client.post("/v1/auth/reset-password", json=payload)
    assert response.status_code == 422
    assert (await VerificationCode.get(code.id)).used is False
    assert (await login(client)).status_code == 200
