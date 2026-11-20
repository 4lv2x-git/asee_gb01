from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class ApiModel(BaseModel):
    model_config = ConfigDict(populate_by_name=True)


class SocialLinks(ApiModel):
    instagram: str | None = None
    twitter: str | None = None
    web: str | None = None


class Song(ApiModel):
    titulo: str
    album: str | None = None
    uri_spotify: str | None = Field(default=None, alias="uriSpotify")


class ArtistSummary(ApiModel):
    id: str
    nombre: str
    imagen: str | None = None


class ArtistDetail(ArtistSummary):
    biografia: str
    redes_sociales: SocialLinks = Field(
        default_factory=SocialLinks, alias="redesSociales"
    )
    eventos_vinculados: list[str] = Field(
        default_factory=list, alias="eventosVinculados"
    )
    canciones_destacadas: list[Song] = Field(
        default_factory=list, alias="cancionesDestacadas"
    )


class ArtistRegistration(ApiModel):
    nombre: str
    biografia: str
    imagen: str | None = None
    redes_sociales: dict[str, str] = Field(
        default_factory=dict, alias="redesSociales"
    )
    eventos_vinculados: list[str] = Field(
        default_factory=list, alias="eventosVinculados"
    )


class ArtistBatchRequest(ApiModel):
    ids: list[str]


class ArticleSummary(ApiModel):
    id: str
    titulo: str
    resumen: str
    imagen: str | None = None
    autor: str
    fecha_publicacion: datetime = Field(alias="fechaPublicacion")


class ArticleDetail(ArticleSummary):
    contenido: str
