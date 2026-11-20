from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.api.routes import admin, auth, health, internal, profile
from app.core.config import get_settings
from app.core.exceptions import AppError
from app.core.logging import configure_logging
from app.db.indexes import ensure_indexes
from app.db.mongodb import close_db, init_db
from app.db.seed import seed_admin


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = get_settings()
    configure_logging()
    await init_db(settings)
    await ensure_indexes()
    await seed_admin(settings)
    yield
    await close_db()


def create_app() -> FastAPI:
    settings = get_settings()
    docs_url = None if settings.app_env == "production" else "/docs"
    openapi_url = None if settings.app_env == "production" else "/openapi.json"
    app = FastAPI(
        title="Gestor de Usuarios e Identificacion - Subsonic Festival",
        version="3.2.0",
        lifespan=lifespan,
        docs_url=docs_url,
        openapi_url=openapi_url,
        openapi_tags=[
            {"name": "Autenticacion y Registro"},
            {"name": "Mi Perfil (Cliente / Proveedor)"},
            {"name": "Administracion"},
            {"name": "Uso Interno"},
        ],
    )
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.cors_origins,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    @app.exception_handler(AppError)
    async def app_error_handler(_request: Request, exc: AppError):
        return JSONResponse(
            status_code=exc.status_code,
            content={"code": exc.code, "message": exc.message, "details": exc.details},
        )

    app.include_router(health.router)
    app.include_router(auth.router, prefix=settings.api_prefix)
    app.include_router(profile.router, prefix=settings.api_prefix)
    app.include_router(admin.router, prefix=settings.api_prefix)
    app.include_router(internal.router, prefix=settings.api_prefix)
    return app


app = create_app()
