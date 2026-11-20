import httpx

from app.core.config import get_settings
from app.core.exceptions import BadGatewayError


class SpacesClient:
    async def get_espacios_by_proveedor(self, proveedor_id: str) -> list[dict]:
        settings = get_settings()
        url = f"{settings.spaces_service_url}/interno/proveedores/{proveedor_id}/espacios"
        try:
            async with httpx.AsyncClient(timeout=settings.spaces_timeout_seconds) as client:
                response = await client.get(url, headers={"X-Internal-Key": settings.internal_api_key})
            response.raise_for_status()
            data = response.json()
            return data if isinstance(data, list) else [data]
        except httpx.HTTPError as exc:
            raise BadGatewayError("Gestor de espacios no disponible") from exc


class FakeSpacesClient:
    def __init__(self):
        self.called_with: str | None = None

    async def get_espacios_by_proveedor(self, proveedor_id: str) -> list[dict]:
        self.called_with = proveedor_id
        return []
