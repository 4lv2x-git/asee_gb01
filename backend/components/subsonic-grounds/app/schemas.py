from typing import List, Optional
from pydantic import BaseModel, ConfigDict, Field
from .models import EstadoEspacio

class EspacioInput(BaseModel):
    evento_id: str
    tipo: str
    dimensiones: str
    ubicacion: Optional[str] = None
    servicios_incluidos: Optional[List[str]] = Field(default_factory=list)
    precio: float
    estado: EstadoEspacio = EstadoEspacio.disponible

class Espacio(BaseModel):
    id: str
    evento_id: str
    tipo: str
    dimensiones: str
    ubicacion: Optional[str] = None
    servicios_incluidos: List[str] = Field(default_factory=list)
    precio: float
    estado: EstadoEspacio
    proveedor_id: Optional[str] = None

    model_config = ConfigDict(from_attributes=True)

class AlquilerRequest(BaseModel):
    proveedor_id: str = Field(..., description="Identificador del proveedor que realiza la reserva.")