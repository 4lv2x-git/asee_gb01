from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel, Field

class UbicacionSchema(BaseModel):
    nombre: Optional[str] = None
    direccion: Optional[str] = None
    ciudad: Optional[str] = None
    pais: Optional[str] = None
    latitud: Optional[float] = None
    longitud: Optional[float] = None

class EventoCreacion(BaseModel):
    nombre: str
    descripcion: Optional[str] = None
    fechaInicio: datetime
    fechaFin: datetime
    ubicacion: Optional[UbicacionSchema] = None
    imagenUrl: Optional[str] = None
    aforo: Optional[int] = None
    estado: Optional[str] = "borrador"

class EventoEdicion(BaseModel):
    nombre: Optional[str] = None
    descripcion: Optional[str] = None
    fechaInicio: Optional[datetime] = None
    fechaFin: Optional[datetime] = None
    ubicacion: Optional[UbicacionSchema] = None
    imagenUrl: Optional[str] = None
    aforo: Optional[int] = None
    estado: Optional[str] = None

class EventoResumen(BaseModel):
    id: str
    nombre: str
    descripcion: Optional[str] = None
    fechaInicio: datetime
    fechaFin: datetime
    ubicacion: Optional[UbicacionSchema] = None
    imagenUrl: Optional[str] = None
    estado: str
    aforo: Optional[int] = None

    class Config:
        from_attributes = True

class SlotSchema(BaseModel):
    id: str
    eventoId: str
    artistaId: str
    nombreArtista: Optional[str] = None
    imagenUrl: Optional[str] = None
    fecha: str
    horaInicio: str
    horaFin: str
    escenario: Optional[str] = None
    orden: Optional[int] = 0

    class Config:
        from_attributes = True

class SlotCreacion(BaseModel):
    artistaId: str
    nombreArtista: Optional[str] = None
    imagenUrl: Optional[str] = None
    fecha: str
    horaInicio: str
    horaFin: str
    escenario: Optional[str] = None
    orden: Optional[int] = 0

class SlotEdicion(BaseModel):
    artistaId: Optional[str] = None
    nombreArtista: Optional[str] = None
    imagenUrl: Optional[str] = None
    fecha: Optional[str] = None
    horaInicio: Optional[str] = None
    horaFin: Optional[str] = None
    escenario: Optional[str] = None
    orden: Optional[int] = None

class PaseSchema(BaseModel):
    id: str
    eventoId: str
    nombre: str
    descripcion: Optional[str] = None
    tipo: Optional[str] = "general"
    precio: float
    moneda: Optional[str] = "EUR"
    stock: int
    stockReservado: Optional[int] = 0
    serviciosIncluidos: Optional[List[str]] = []
    estado: Optional[str] = "disponible"
    fechaInicioVenta: Optional[datetime] = None
    fechaFinVenta: Optional[datetime] = None

    class Config:
        from_attributes = True

class PaseCreacion(BaseModel):
    nombre: str
    descripcion: Optional[str] = None
    tipo: Optional[str] = "general"
    precio: float
    moneda: Optional[str] = "EUR"
    stock: int = Field(..., ge=0)
    serviciosIncluidos: Optional[List[str]] = []
    estado: Optional[str] = "disponible"
    fechaInicioVenta: Optional[datetime] = None
    fechaFinVenta: Optional[datetime] = None

class PaseEdicion(BaseModel):
    nombre: Optional[str] = None
    descripcion: Optional[str] = None
    tipo: Optional[str] = None
    precio: Optional[float] = None
    moneda: Optional[str] = None
    stock: Optional[int] = Field(None, ge=0)
    serviciosIncluidos: Optional[List[str]] = None
    estado: Optional[str] = None
    fechaInicioVenta: Optional[datetime] = None
    fechaFinVenta: Optional[datetime] = None

class PaginatedEventos(BaseModel):
    data: List[EventoResumen]
    paginacion: dict

class AjusteStock(BaseModel):
    operacion: str
    cantidad: int = Field(..., ge=0)

class PaseStock(BaseModel):
    eventoId: str
    paseId: str
    stockAnterior: int
    stockActual: int
    operacion: str
    cantidad: int

class DisponibilidadPase(BaseModel):
    eventoId: str
    paseId: str
    existeEvento: bool
    existePase: bool
    disponible: bool
    stockActual: int
    cantidadSolicitada: int

class ArtistaIdsResponse(BaseModel):
    eventoId: str
    artistaIds: List[str]