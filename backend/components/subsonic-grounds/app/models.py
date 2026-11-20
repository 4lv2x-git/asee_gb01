import uuid
import enum
from sqlalchemy import Column, String, Float, Enum, JSON
from .database import Base

class EstadoEspacio(str, enum.Enum):
    disponible = "disponible"
    reservado = "reservado"
    ocupado = "ocupado"

class EspacioModel(Base):
    __tablename__ = "espacios"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()), index=True)
    evento_id = Column(String, nullable=False, index=True)
    tipo = Column(String, nullable=False, index=True)
    dimensiones = Column(String, nullable=False)
    ubicacion = Column(String, nullable=True, index=True)
    servicios_incluidos = Column(JSON, nullable=False, default=list)
    precio = Column(Float, nullable=False)
    estado = Column(Enum(EstadoEspacio), nullable=False, default=EstadoEspacio.disponible)
    proveedor_id = Column(String, nullable=True, index=True)