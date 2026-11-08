import logging
from contextlib import asynccontextmanager
from typing import Annotated, AsyncIterator

import jwt
from fastapi import (
    Depends,
    FastAPI,
    HTTPException,
    Query,
    Request,
    Response,
    status,
)
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from pymongo import MongoClient
from pymongo.database import Database
from pymongo.errors import PyMongoError

from src.config import Settings
from src.database import connect, get_database
from src.repositories import ArtistRepository, ArticleRepository
from src.schemas import (
    ArticleDetail,
    ArticleSummary,
    ArtistBatchRequest,
    ArtistDetail,
    ArtistRegistration,
    ArtistSummary,
    Song,
)
from src.services import ArtistService, BlogService

logger = logging.getLogger("artists_blog")
bearer_scheme = HTTPBearer(auto_error=False)


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    settings: Settings = app.state.settings
    client = connect(settings)
    app.state.mongo_client = client
    try:
        yield
    finally:
        client.close()


def create_app(settings: Settings | None = None) -> FastAPI:
    app = FastAPI(
        title="Artists & Blog",
        description="Servicio independiente de artistas y publicaciones del festival.",
        version="1.0.0",
        lifespan=lifespan,
    )
    app.state.settings = settings or Settings.from_environment()

    @app.exception_handler(PyMongoError)
    async def database_error_handler(
        _request: Request, error: PyMongoError
    ) -> Response:
        logger.error(
            "MongoDB request failed: %s",
            error,
            exc_info=(type(error), error, error.__traceback__),
        )
        return Response(
            content='{"detail":"Database service unavailable"}',
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            media_type="application/json",
        )

    def database(request: Request) -> Database:
        client: MongoClient = request.app.state.mongo_client
        return get_database(client, request.app.state.settings)

    def artists(db: Annotated[Database, Depends(database)]) -> ArtistService:
        return ArtistService(ArtistRepository(db["artistas"]))

    def blog(db: Annotated[Database, Depends(database)]) -> BlogService:
        return BlogService(ArticleRepository(db["articulos"]))

    def require_role(role: str):
        def authorize(
            request: Request,
            credentials: Annotated[
                HTTPAuthorizationCredentials | None, Depends(bearer_scheme)
            ],
        ) -> None:
            if credentials is None:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Bearer token required",
                    headers={"WWW-Authenticate": "Bearer"},
                )
            service_settings: Settings = request.app.state.settings
            if service_settings.jwt_secret is None:
                raise HTTPException(
                    status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                    detail="JWT verification is not configured",
                )
            try:
                claims = jwt.decode(
                    credentials.credentials,
                    service_settings.jwt_secret,
                    algorithms=[service_settings.jwt_algorithm],
                )
            except jwt.PyJWTError as error:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Invalid bearer token",
                    headers={"WWW-Authenticate": "Bearer"},
                ) from error

            scopes = claims.get("scope", "")
            if isinstance(scopes, str):
                scopes = scopes.split()
            elif not isinstance(scopes, list):
                scopes = []
            roles = claims.get("roles", [])
            if isinstance(roles, str):
                roles = roles.split()
            elif not isinstance(roles, list):
                roles = []
            if role not in roles and role not in scopes:
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail=f"Required role: {role}",
                )

        return authorize

    @app.get("/health", tags=["Health"])
    def health(db: Annotated[Database, Depends(database)]) -> dict[str, str]:
        try:
            db.command("ping")
        except PyMongoError as error:
            logger.warning("MongoDB health check failed: %s", error)
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Database service unavailable",
            ) from error
        return {"status": "ok", "database": "ok"}

    @app.get("/v1/artistas", response_model=list[ArtistSummary], tags=["Artists"])
    def list_artists(
        service: Annotated[ArtistService, Depends(artists)],
    ) -> list[ArtistSummary]:
        return service.list_artists()

    @app.get(
        "/v1/artistas/{artista_id}",
        response_model=ArtistDetail,
        tags=["Artists"],
    )
    def get_artist(
        artista_id: str,
        service: Annotated[ArtistService, Depends(artists)],
    ) -> ArtistDetail:
        artist = service.get_artist(artista_id)
        if artist is None:
            raise HTTPException(status_code=404, detail="Artist not found")
        return artist

    @app.get(
        "/v1/artistas/{artista_id}/canciones",
        response_model=list[Song],
        tags=["Artists"],
    )
    def get_artist_songs(
        artista_id: str,
        service: Annotated[ArtistService, Depends(artists)],
    ) -> list[Song]:
        songs = service.get_songs(artista_id)
        if songs is None:
            raise HTTPException(status_code=404, detail="Artist not found")
        return songs

    @app.post(
        "/v1/artistas/interno/lote",
        response_model=list[ArtistDetail],
        tags=["Artists"],
        dependencies=[Depends(require_role("servicio_interno"))],
    )
    def get_artist_batch(
        payload: ArtistBatchRequest,
        service: Annotated[ArtistService, Depends(artists)],
    ) -> list[ArtistDetail]:
        return service.get_artists(payload.ids)

    @app.post(
        "/v1/artistas/admin",
        response_model=ArtistDetail,
        status_code=status.HTTP_201_CREATED,
        tags=["Artists"],
        dependencies=[Depends(require_role("admin"))],
    )
    def create_artist(
        payload: ArtistRegistration,
        service: Annotated[ArtistService, Depends(artists)],
    ) -> ArtistDetail:
        return service.create_artist(payload)

    @app.put(
        "/v1/artistas/admin/{artista_id}",
        response_model=ArtistDetail,
        tags=["Artists"],
        dependencies=[Depends(require_role("admin"))],
    )
    def update_artist(
        artista_id: str,
        payload: ArtistRegistration,
        service: Annotated[ArtistService, Depends(artists)],
    ) -> ArtistDetail:
        artist = service.update_artist(artista_id, payload)
        if artist is None:
            raise HTTPException(status_code=404, detail="Artist not found")
        return artist

    @app.delete(
        "/v1/artistas/admin/{artista_id}",
        status_code=status.HTTP_204_NO_CONTENT,
        tags=["Artists"],
        dependencies=[Depends(require_role("admin"))],
    )
    def delete_artist(
        artista_id: str,
        service: Annotated[ArtistService, Depends(artists)],
    ) -> Response:
        if not service.delete_artist(artista_id):
            raise HTTPException(status_code=404, detail="Artist not found")
        return Response(status_code=status.HTTP_204_NO_CONTENT)

    @app.get("/v1/blog", response_model=list[ArticleSummary], tags=["Blog"])
    def list_articles(
        service: Annotated[BlogService, Depends(blog)],
        skip: Annotated[int, Query(ge=0)] = 0,
        limit: Annotated[int, Query(ge=1, le=100)] = 20,
    ) -> list[ArticleSummary]:
        return service.list_articles(skip, limit)

    @app.get(
        "/v1/blog/{articulo_id}",
        response_model=ArticleDetail,
        tags=["Blog"],
    )
    def get_article(
        articulo_id: str,
        service: Annotated[BlogService, Depends(blog)],
    ) -> ArticleDetail:
        article = service.get_article(articulo_id)
        if article is None:
            raise HTTPException(status_code=404, detail="Article not found")
        return article

    return app


app = create_app()
