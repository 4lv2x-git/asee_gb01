from typing import List, Optional
from fastapi import FastAPI, Depends, Header, HTTPException, Path, Query, status
from sqlalchemy.orm import Session
from . import models, schemas, crud
from .database import engine, get_db

# Crear tablas al iniciar
models.Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Gestor de Espacios - Subsonic Festival",
    description=(
        "Módulo encargado de administrar los espacios que los proveedores pueden alquilar "
        "dentro de cada evento. Controla la disponibilidad, precios y vinculación de reservas."
    ),
    version="1.0.0"
)

# ==========================================
# DEPENDENCIA DE SEGURIDAD Y ROLES
# ==========================================
def verificar_roles(roles_permitidos: List[str]):
    def validador(x_user_role: str = Header(default="admin", alias="X-User-Role")):
        if x_user_role.lower() not in roles_permitidos:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"Se requiere uno de los siguientes roles: {', '.join(roles_permitidos)}"
            )
        return x_user_role
    return validador

# ==========================================
# RUTAS EXACTAS DE ESPACIOS.YAML (/api/grounds)
# ==========================================
@app.get(
    "/api/grounds",
    response_model=List[schemas.Espacio],
    tags=["Recintos y Espacios"],
    summary="Listar y buscar espacios"
)
def listar_y_buscar_espacios(
    tipo: Optional[str] = Query(None, description="Filtrar por tipo de espacio o servicio (ej. comida, merchandising)."),
    ubicacion: Optional[str] = Query(None, description="Filtrar por ubicación dentro del festival."),
    precio_max: Optional[float] = Query(None, description="Filtrar por precio máximo de alquiler."),
    db: Session = Depends(get_db)
):
    return crud.listar_espacios(db, tipo=tipo, ubicacion=ubicacion, precio_max=precio_max)

@app.post(
    "/api/grounds",
    response_model=schemas.Espacio,
    status_code=status.HTTP_201_CREATED,
    tags=["Recintos y Espacios"],
    summary="Registrar un nuevo espacio o recinto",
    dependencies=[Depends(verificar_roles(["admin", "proveedor"]))]
)
def registrar_espacio(datos: schemas.EspacioInput, db: Session = Depends(get_db)):
    return crud.crear_espacio(db, datos)

@app.put(
    "/api/grounds/{ground_id}",
    response_model=schemas.Espacio,
    tags=["Recintos y Espacios"],
    summary="Modificar los detalles de un espacio",
    dependencies=[Depends(verificar_roles(["admin", "proveedor"]))]
)
def modificar_espacio(
    ground_id: str = Path(..., description="Identificador único del espacio."),
    datos: schemas.EspacioInput = ...,
    db: Session = Depends(get_db)
):
    return crud.actualizar_espacio(db, ground_id, datos)

@app.delete(
    "/api/grounds/{ground_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    tags=["Recintos y Espacios"],
    summary="Eliminar un espacio",
    dependencies=[Depends(verificar_roles(["admin", "proveedor"]))]
)
def eliminar_espacio(
    ground_id: str = Path(..., description="Identificador único del espacio."),
    db: Session = Depends(get_db)
):
    crud.eliminar_espacio(db, ground_id)
    return None

@app.post(
    "/api/grounds/{ground_id}/alquilar",
    response_model=schemas.Espacio,
    tags=["Recintos y Espacios"],
    summary="Registrar el alquiler de un espacio",
    dependencies=[Depends(verificar_roles(["proveedor"]))]
)
def alquilar_espacio(
    ground_id: str = Path(..., description="Identificador único del espacio."),
    solicitud: schemas.AlquilerRequest = ...,
    db: Session = Depends(get_db)
):
    return crud.alquilar_espacio(db, ground_id, solicitud.proveedor_id)