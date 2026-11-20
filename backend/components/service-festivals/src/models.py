from sqlalchemy import Column, Integer, String, Float, DateTime, Text, ForeignKey, JSON
from sqlalchemy.orm import relationship
from .database import Base

class EventoModel(Base):
    __tablename__ = "eventos"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    nombre = Column(String(150), nullable=False, index=True)
    descripcion = Column(Text, nullable=True)
    fecha_inicio = Column(DateTime, nullable=False)
    fecha_fin = Column(DateTime, nullable=False)
    
    # Datos de Ubicación
    ubicacion_nombre = Column(String(100), nullable=True)
    ubicacion_direccion = Column(String(200), nullable=True)
    ubicacion_ciudad = Column(String(100), nullable=True)
    ubicacion_pais = Column(String(100), nullable=True)
    ubicacion_latitud = Column(Float, nullable=True)
    ubicacion_longitud = Column(Float, nullable=True)

    imagen_url = Column(String(255), nullable=True)
    estado = Column(String(50), default="borrador")  # borrador, publicado, cancelado, finalizado
    aforo = Column(Integer, nullable=True)

    # Relaciones relacionales
    escaleta = relationship("SlotModel", back_populates="evento", cascade="all, delete-orphan")
    pases = relationship("PaseModel", back_populates="evento", cascade="all, delete-orphan")


class SlotModel(Base):
    __tablename__ = "slots"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    evento_id = Column(Integer, ForeignKey("eventos.id"), nullable=False)
    artista_id = Column(String(100), nullable=False)
    nombre_artista = Column(String(150), nullable=True)
    imagen_url = Column(String(255), nullable=True)
    fecha = Column(String(50), nullable=False)
    hora_inicio = Column(String(50), nullable=False)
    hora_fin = Column(String(50), nullable=False)
    escenario = Column(String(100), nullable=True)
    orden = Column(Integer, default=0)

    evento = relationship("EventoModel", back_populates="escaleta")


class PaseModel(Base):
    __tablename__ = "pases"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    evento_id = Column(Integer, ForeignKey("eventos.id"), nullable=False)
    nombre = Column(String(100), nullable=False)
    descripcion = Column(Text, nullable=True)
    tipo = Column(String(50), default="general")  # general, vip, premium, abono, otro
    precio = Column(Float, nullable=False)
    moneda = Column(String(10), default="EUR")
    stock = Column(Integer, nullable=False, default=0)
    stock_reservado = Column(Integer, default=0)
    servicios_incluidos = Column(JSON, nullable=True)
    estado = Column(String(50), default="disponible")  # disponible, agotado, inactivo
    fecha_inicio_venta = Column(DateTime, nullable=True)
    fecha_fin_venta = Column(DateTime, nullable=True)

    evento = relationship("EventoModel", back_populates="pases")