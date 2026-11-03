from sqlalchemy.orm import Session
from . import models, schemas

# --- EVENTOS ---
def get_evento(db: Session, evento_id: int):
    return db.query(models.EventoModel).filter(models.EventoModel.id == evento_id).first()

def get_eventos(db: Session, skip: int = 0, limit: int = 20, estado: str = None, ubicacion: str = None):
    query = db.query(models.EventoModel)
    if estado:
        query = query.filter(models.EventoModel.estado == estado)
    if ubicacion:
        query = query.filter(models.EventoModel.ubicacion_ciudad.ilike(f"%{ubicacion}%"))
    total = query.count()
    items = query.offset(skip).limit(limit).all()
    return items, total

def create_evento(db: Session, evento: schemas.EventoCreacion):
    ubicacion = evento.ubicacion or schemas.UbicacionSchema()
    db_evento = models.EventoModel(
        nombre=evento.nombre,
        descripcion=evento.descripcion,
        fecha_inicio=evento.fechaInicio,
        fecha_fin=evento.fechaFin,
        ubicacion_nombre=ubicacion.nombre,
        ubicacion_direccion=ubicacion.direccion,
        ubicacion_ciudad=ubicacion.ciudad,
        ubicacion_pais=ubicacion.pais,
        ubicacion_latitud=ubicacion.latitud,
        ubicacion_longitud=ubicacion.longitud,
        imagen_url=evento.imagenUrl,
        estado=evento.estado or "borrador",
        aforo=evento.aforo
    )
    db.add(db_evento)
    db.commit()
    db.refresh(db_evento)
    return db_evento

def update_evento(db: Session, evento_id: int, evento_data: schemas.EventoEdicion):
    db_evento = get_evento(db, evento_id)
    if not db_evento:
        return None
    update_data = evento_data.model_dump(exclude_unset=True)
    if "nombre" in update_data: db_evento.nombre = update_data["nombre"]
    if "descripcion" in update_data: db_evento.descripcion = update_data["descripcion"]
    if "fechaInicio" in update_data: db_evento.fecha_inicio = update_data["fechaInicio"]
    if "fechaFin" in update_data: db_evento.fecha_fin = update_data["fechaFin"]
    if "imagenUrl" in update_data: db_evento.imagen_url = update_data["imagenUrl"]
    if "estado" in update_data: db_evento.estado = update_data["estado"]
    if "aforo" in update_data: db_evento.aforo = update_data["aforo"]
    if evento_data.ubicacion:
        u = evento_data.ubicacion
        if u.nombre is not None: db_evento.ubicacion_nombre = u.nombre
        if u.direccion is not None: db_evento.ubicacion_direccion = u.direccion
        if u.ciudad is not None: db_evento.ubicacion_ciudad = u.ciudad
        if u.pais is not None: db_evento.ubicacion_pais = u.pais
        if u.latitud is not None: db_evento.ubicacion_latitud = u.latitud
        if u.longitud is not None: db_evento.ubicacion_longitud = u.longitud

    db.commit()
    db.refresh(db_evento)
    return db_evento

def delete_evento(db: Session, evento_id: int):
    db_evento = get_evento(db, evento_id)
    if db_evento:
        db.delete(db_evento)
        db.commit()
    return db_evento

# --- ESCALETA (SLOTS) ---
def get_slots(db: Session, evento_id: int):
    return db.query(models.SlotModel).filter(models.SlotModel.evento_id == evento_id).all()

def create_slot(db: Session, evento_id: int, slot: schemas.SlotCreacion):
    db_slot = models.SlotModel(
        evento_id=evento_id,
        artista_id=slot.artistaId,
        nombre_artista=slot.nombreArtista,
        imagen_url=slot.imagenUrl,
        fecha=slot.fecha,
        hora_inicio=slot.horaInicio,
        hora_fin=slot.horaFin,
        escenario=slot.escenario,
        orden=slot.orden or 0
    )
    db.add(db_slot)
    db.commit()
    db.refresh(db_slot)
    return db_slot

def update_slot(db: Session, slot_id: int, slot_data: schemas.SlotEdicion):
    db_slot = db.query(models.SlotModel).filter(models.SlotModel.id == slot_id).first()
    if not db_slot:
        return None
    update_data = slot_data.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        db_key = "artista_id" if key == "artistaId" else ("nombre_artista" if key == "nombreArtista" else ("imagen_url" if key == "imagenUrl" else ("hora_inicio" if key == "horaInicio" else ("hora_fin" if key == "horaFin" else key))))
        setattr(db_slot, db_key, value)
    db.commit()
    db.refresh(db_slot)
    return db_slot

def delete_slot(db: Session, slot_id: int):
    db_slot = db.query(models.SlotModel).filter(models.SlotModel.id == slot_id).first()
    if db_slot:
        db.delete(db_slot)
        db.commit()
    return db_slot

# --- PASES ---
def get_pases(db: Session, evento_id: int):
    return db.query(models.PaseModel).filter(models.PaseModel.evento_id == evento_id).all()

def get_pase(db: Session, pase_id: int):
    return db.query(models.PaseModel).filter(models.PaseModel.id == pase_id).first()

def create_pase(db: Session, evento_id: int, pase: schemas.PaseCreacion):
    db_pase = models.PaseModel(
        evento_id=evento_id,
        nombre=pase.nombre,
        descripcion=pase.descripcion,
        tipo=pase.tipo or "general",
        precio=pase.precio,
        moneda=pase.moneda or "EUR",
        stock=pase.stock,
        servicios_incluidos=pase.serviciosIncluidos,
        estado=pase.estado or "disponible",
        fecha_inicio_venta=pase.fechaInicioVenta,
        fecha_fin_venta=pase.fechaFinVenta
    )
    db.add(db_pase)
    db.commit()
    db.refresh(db_pase)
    return db_pase

def update_pase(db: Session, pase_id: int, pase_data: schemas.PaseEdicion):
    db_pase = get_pase(db, pase_id)
    if not db_pase:
        return None
    update_data = pase_data.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        db_key = "stock_reservado" if key == "stockReservado" else ("servicios_incluidos" if key == "serviciosIncluidos" else ("fecha_inicio_venta" if key == "fechaInicioVenta" else ("fecha_fin_venta" if key == "fechaFinVenta" else key)))
        setattr(db_pase, db_key, value)
    db.commit()
    db.refresh(db_pase)
    return db_pase

def delete_pase(db: Session, pase_id: int):
    db_pase = get_pase(db, pase_id)
    if db_pase:
        db.delete(db_pase)
        db.commit()
    return db_pase