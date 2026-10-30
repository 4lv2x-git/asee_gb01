from typing import List, Optional
from sqlalchemy.orm import Session
from fastapi import HTTPException, status
from . import models, schemas

def listar_espacios(
    db: Session,
    tipo: Optional[str] = None,
    evento_id: Optional[str] = None,
    ubicacion: Optional[str] = None,
    precio_max: Optional[float] = None
) -> List[models.EspacioModel]:
    query = db.query(models.EspacioModel)

    if tipo:
        query = query.filter(models.EspacioModel.tipo.ilike(f"%{tipo}%"))
    if evento_id:
        query = query.filter(models.EspacioModel.evento_id == evento_id)
    if ubicacion:
        query = query.filter(models.EspacioModel.ubicacion.ilike(f"%{ubicacion}%"))
    if precio_max is not None:
        query = query.filter(models.EspacioModel.precio <= precio_max)

    return query.all()

def obtener_espacio(db: Session, space_id: str) -> models.EspacioModel:
    espacio = db.query(models.EspacioModel).filter(models.EspacioModel.id == space_id).first()
    if not espacio:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Espacio no encontrado"
        )
    return espacio

def crear_espacio(db: Session, datos: schemas.EspacioInput) -> models.EspacioModel:
    nuevo = models.EspacioModel(
        evento_id=datos.evento_id,
        tipo=datos.tipo,
        dimensiones=datos.dimensiones,
        ubicacion=datos.ubicacion,
        servicios_incluidos=datos.servicios_incluidos or [],
        precio=datos.precio,
        estado=datos.estado
    )
    db.add(nuevo)
    db.commit()
    db.refresh(nuevo)
    return nuevo

def actualizar_espacio(db: Session, space_id: str, datos: schemas.EspacioInput) -> models.EspacioModel:
    espacio = obtener_espacio(db, space_id)
    espacio.evento_id = datos.evento_id
    espacio.tipo = datos.tipo
    espacio.dimensiones = datos.dimensiones
    espacio.ubicacion = datos.ubicacion
    espacio.servicios_incluidos = datos.servicios_incluidos or []
    espacio.precio = datos.precio
    espacio.estado = datos.estado

    db.commit()
    db.refresh(espacio)
    return espacio

def eliminar_espacio(db: Session, space_id: str) -> None:
    espacio = obtener_espacio(db, space_id)
    db.delete(espacio)
    db.commit()

def alquilar_espacio(db: Session, space_id: str, proveedor_id: str) -> models.EspacioModel:
    espacio = obtener_espacio(db, space_id)
    if espacio.estado != models.EstadoEspacio.disponible:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="El espacio no se encuentra disponible para alquilar"
        )
    espacio.estado = models.EstadoEspacio.reservado
    espacio.proveedor_id = proveedor_id

    db.commit()
    db.refresh(espacio)
    return espacio

def listar_espacios_por_proveedor(db: Session, proveedor_id: str) -> List[models.EspacioModel]:
    return db.query(models.EspacioModel).filter(
        models.EspacioModel.proveedor_id == proveedor_id
    ).all()