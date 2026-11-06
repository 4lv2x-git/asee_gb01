import pytest
from beanie import init_beanie
from httpx import ASGITransport, AsyncClient
from mongomock_motor import AsyncMongoMockClient

from app.main import create_app
from app.models import RevokedToken, User, VerificationCode


@pytest.fixture(autouse=True)
async def mongo():
    client = AsyncMongoMockClient()
    await init_beanie(
        database=client["test_users_auth"],
        document_models=[User, RevokedToken, VerificationCode],
    )
    yield
    client.close()


@pytest.fixture
def app():
    return create_app()


@pytest.fixture
async def client(app):
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        yield ac
