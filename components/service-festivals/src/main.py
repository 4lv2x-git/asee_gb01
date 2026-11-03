from fastapi import FastAPI, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from typing import Optional
from . import models, schemas, crud
from .database import engine, get_db

models.Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Gestor de Eventos (Festivales y Escaleta)",
    version="1.0.0",
    description="Microservicio completo para la gestión de festivales, escaleta y pases según OpenAPI."
)

def format_evento(ev):
    return {
        "id": str(ev.id),
        "nombre": ev.nombre,
        "descripcion": ev.descripcion,
        "fechaInicio": ev.fecha_inicio,
        "fechaFin": ev.fecha_fin,
        "ubicacion": {
            "nombre": ev.ubicacion_nombre,
            "direccion": ev.ubicacion_direccion,
            "ciudad": ev.ubicacion_ciudad,
            "pais": ev.ubicacion_pais,
            "latitud": ev.ubicacion_latitud,
            "longitud": ev.ubicacion_longitud
        } if ev.ubicacion_ciudad else None,
        "imagenUrl": ev.imagen_url,
        "estado": ev.estado,
        "aforo": ev.aforo
    }

def format_slot(slot):
    return {
        "id": str(slot.id),
        "eventoId": str(slot.evento_id),
        "artistaId": slot.artista_id,
        "nombreArtista": slot.nombre_artista,
        "imagenUrl": slot.imagen_url,
        "fecha": slot.fecha,
        "horaInicio": slot.hora_inicio,
        "horaFin": slot.hora_fin,
        "escenario": slot.escenario,
        "orden": slot.orden
    }

def format_pase(pase):
    return {
        "id": str(pase.id),
        "eventoId": str(pase.evento_id),
        "nombre": pase.nombre,
        "descripcion": pase.descripcion,
        "tipo": pase.tipo,
        "precio": pase.precio,
        "moneda": pase.moneda,
        "stock": pase.stock,
        "stockReservado": pase.stock_reservado,
        "serviciosIncluidos": pase.servicios_incluidos or [],
        "estado": pase.estado,
        "fechaInicioVenta": pase.fecha_inicio_venta,
        "fechaFinVenta": pase.fecha_fin_venta
    }

# --- EVENTOS ---
@app.get("/eventos", response_model=schemas.PaginatedEventos)
def listar_eventos(
    nombre: Optional[str] = None,
    ubicacion: Optional[str] = None,
    estado: Optional[str] = None,
    page: int = Query(1, ge=1),
    limit: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db)
):
    skip = (page - 1) * limit
    items, total = crud.get_eventos(db, skip=skip, limit=limit, estado=estado, ubicacion=ubicacion)
    total_pages = (total + limit - 1) // limit if limit > 0 else 1
    
    return {
        "data": [format_evento(ev) for ev in items],
        "paginacion": {"page": page, "limit": limit, "total": total, "totalPages": total_pages}
    }

@app.post("/eventos", status_code=status.HTTP_201_CREATED)
def crear_evento(evento: schemas.EventoCreacion, db: Session = Depends(get_db)):
    ev = crud.create_evento(db, evento)
    return format_evento(ev)

@app.get("/eventos/{evento_id}")
def obtener_detalle_evento(evento_id: int, db: Session = Depends(get_db)):
    ev = crud.get_evento(db, evento_id)
    if not ev:
        raise HTTPException(status_code=404, detail="Evento no encontrado")
    res = format_evento(ev)
    res["escaleta"] = [format_slot(s) for s in ev.escaleta]
    res["pases"] = [format_pase(p) for p in ev.pases]
    res["artistas"] = []
    return res

@app.put("/eventos/{evento_id}")
def actualizar_evento(evento_id: int, evento_data: schemas.EventoEdicion, db: Session = Depends(get_db)):
    ev = crud.update_evento(db, evento_id, evento_data)
    if not ev:
        raise HTTPException(status_code=404, detail="Evento no encontrado")
    return format_evento(ev)

@app.delete("/eventos/{evento_id}", status_code=status.HTTP_204_NO_CONTENT)
def eliminar_evento(evento_id: int, db: Session = Depends(get_db)):
    ev = crud.delete_evento(db, evento_id)
    if not ev:
        raise HTTPException(status_code=404, detail="Evento no encontrado")
    return

@app.patch("/eventos/{evento_id}/estado")
def cambiar_estado_evento(evento_id: int, body: dict, db: Session = Depends(get_db)):
    nuevo_estado = body.get("estado")
    if nuevo_estado not in ["borrador", "publicado", "cancelado", "finalizado"]:
        raise HTTPException(status_code=400, detail="Estado inválido")
    ev = crud.update_evento(db, evento_id, schemas.EventoEdicion(estado=nuevo_estado))
    if not ev:
        raise HTTPException(status_code=404, detail="Evento no encontrado")
    return format_evento(ev)

# --- ESCALETA ---
@app.get("/eventos/{evento_id}/escaleta")
def listar_escaleta(evento_id: int, db: Session = Depends(get_db)):
    if not crud.get_evento(db, evento_id):
        raise HTTPException(status_code=404, detail="Evento no encontrado")
    return {"data": [format_slot(s) for s in crud.get_slots(db, evento_id)]}

@app.post("/eventos/{evento_id}/escaleta", status_code=status.HTTP_201_CREATED)
def crear_slot(evento_id: int, slot: schemas.SlotCreacion, db: Session = Depends(get_db)):
    if not crud.get_evento(db, evento_id):
        raise HTTPException(status_code=404, detail="Evento no encontrado")
    return format_slot(crud.create_slot(db, evento_id, slot))

@app.put("/eventos/{evento_id}/escaleta/{slot_id}")
def actualizar_slot(evento_id: int, slot_id: int, slot_data: schemas.SlotEdicion, db: Session = Depends(get_db)):
    s = crud.update_slot(db, slot_id, slot_data)
    if not s:
        raise HTTPException(status_code=404, detail="Slot no encontrado")
    return format_slot(s)

@app.delete("/eventos/{evento_id}/escaleta/{slot_id}", status_code=status.HTTP_204_NO_CONTENT)
def eliminar_slot(evento_id: int, slot_id: int, db: Session = Depends(get_db)):
    s = crud.delete_slot(db, slot_id)
    if not s:
        raise HTTPException(status_code=404, detail="Slot no encontrado")
    return

# --- PASES ---
@app.get("/eventos/{evento_id}/pases")
def listar_pases(evento_id: int, incluirAgotados: bool = False, db: Session = Depends(get_db)):
    if not crud.get_evento(db, evento_id):
        raise HTTPException(status_code=404, detail="Evento no encontrado")
    pases = crud.get_pases(db, evento_id)
    if not incluirAgotados:
        pases = [p for p in pases if p.estado == "disponible"]
    return {"data": [format_pase(p) for p in pases]}

@app.post("/eventos/{evento_id}/pases", status_code=status.HTTP_201_CREATED)
def crear_pase(evento_id: int, pase: schemas.PaseCreacion, db: Session = Depends(get_db)):
    if not crud.get_evento(db, evento_id):
        raise HTTPException(status_code=404, detail="Evento no encontrado")
    return format_pase(crud.create_pase(db, evento_id, pase))

@app.get("/eventos/{evento_id}/pases/{pase_id}")
def obtener_pase(evento_id: int, pase_id: int, db: Session = Depends(get_db)):
    p = crud.get_pase(db, pase_id)
    if not p or p.evento_id != evento_id:
        raise HTTPException(status_code=404, detail="Pase no encontrado")
    return format_pase(p)

@app.put("/eventos/{evento_id}/pases/{pase_id}")
def actualizar_pase(evento_id: int, pase_id: int, pase_data: schemas.PaseEdicion, db: Session = Depends(get_db)):
    p = crud.get_pase(db, pase_id)
    if not p or p.evento_id != evento_id:
        raise HTTPException(status_code=404, detail="Pase no encontrado")
    return format_pase(crud.update_pase(db, pase_id, pase_data))

@app.delete("/eventos/{evento_id}/pases/{pase_id}", status_code=status.HTTP_204_NO_CONTENT)
def eliminar_pase(evento_id: int, pase_id: int, db: Session = Depends(get_db)):
    p = crud.get_pase(db, pase_id)
    if not p or p.evento_id != evento_id:
        raise HTTPException(status_code=404, detail="Pase no encontrado")
    crud.delete_pase(db, pase_id)
    return

# --- USO INTERNO ---
@app.get("/eventos/{evento_id}/pases/{pase_id}/disponibilidad")
def consultar_disponibilidad(evento_id: int, pase_id: int, cantidad: int = 1, db: Session = Depends(get_db)):
    ev = crud.get_evento(db, evento_id)
    p = crud.get_pase(db, pase_id)
    existe_evento = ev is not None
    existe_pase = p is not None and p.evento_id == evento_id
    
    disponible = existe_pase and p.stock >= cantidad and p.estado == "disponible"
    return {
        "eventoId": str(evento_id),
        "paseId": str(pase_id),
        "existeEvento": existe_evento,
        "existePase": existe_pase,
        "disponible": disponible,
        "stockActual": p.stock if p else 0,
        "cantidadSolicitada": cantidad
    }

@app.patch("/eventos/{evento_id}/pases/{pase_id}/stock")
def ajustar_stock(evento_id: int, pase_id: int, ajuste: schemas.AjusteStock, db: Session = Depends(get_db)):
    p = crud.get_pase(db, pase_id)
    if not p or p.evento_id != evento_id:
        raise HTTPException(status_code=404, detail="Pase no encontrado")
    
    stock_anterior = p.stock
    if ajuste.operacion == "decrementar":
        if stock_anterior < ajuste.cantidad:
            raise HTTPException(status_code=409, detail="Stock insuficiente")
        p.stock -= ajuste.cantidad
    elif ajuste.operacion == "incrementar":
        p.stock += ajuste.cantidad
    elif ajuste.operacion == "establecer":
        p.stock = ajuste.cantidad
    else:
        raise HTTPException(status_code=400, detail="Operación inválida")

    p.estado = "agotado" if p.stock == 0 else "disponible"
    db.commit()
    db.refresh(p)

    return {
        "eventoId": str(evento_id),
        "paseId": str(pase_id),
        "stockAnterior": stock_anterior,
        "stockActual": p.stock,
        "operacion": ajuste.operacion,
        "cantidad": ajuste.cantidad
    }

@app.get("/eventos/{evento_id}/artistas-ids")
def obtener_artistas_ids(evento_id: int, db: Session = Depends(get_db)):
    if not crud.get_evento(db, evento_id):
        raise HTTPException(status_code=404, detail="Evento no encontrado")
    slots = crud.get_slots(db, evento_id)
    return {
        "eventoId": str(evento_id),
        "artistaIds": list(set([s.artista_id for s in slots if s.artista_id]))
    }