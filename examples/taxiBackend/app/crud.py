from sqlalchemy.orm import Session
from sqlalchemy import select, delete, exists, func
import random
from . import models, schemas

def _require_fields(data: dict, *fields: str):
    missing = [f for f in fields if not data.get(f)]
    if missing:
        raise ValueError(f"missing fields: {', '.join(missing)}")

def _normalize_id(v):
    if isinstance(v, str):
        if v.isdigit():
            return int(v)
        return None
    return v

def _name_exists(db: Session, model, name: str) -> bool:
    return db.scalar(select(exists().where(func.lower(model.name) == name.lower())))

def _resolve_single(db: Session, model, tag: str, *, obj_id=None, obj_name=None):
    obj_id = _normalize_id(obj_id)
    if obj_id is not None:
        obj = db.get(model, obj_id)
        if obj is None:
            raise ValueError(f"{tag} id {obj_id} not found")
        return obj
    if obj_name is not None:
        matches = db.scalars(select(model).where(func.lower(model.name) == obj_name.lower())).all()
        if not matches:
            raise ValueError(f"no {tag}s named '{obj_name}'")
        if len(matches) > 1:
            detail = "\n".join(
                f"{m.id}: {getattr(m, 'name', '')} {getattr(m, 'car_plate', '')}".strip()
                for m in matches
            )
            raise ValueError(
                f"ambiguous name '{obj_name}'; multiple {tag}s found:\n{detail}"
            )
        return matches[0]
    raise ValueError(f"{tag} id or name required")

def create_rider(db: Session, data: schemas.RiderCreate) -> models.Rider:
    _require_fields(data.model_dump(), "name")
    rider = models.Rider(**data.model_dump())
    db.add(rider)
    db.commit()
    db.refresh(rider)
    return rider

def create_driver(db: Session, data: schemas.DriverCreate) -> models.Driver:
    _require_fields(data.model_dump(), "name", "car_plate")
    if _name_exists(db, models.Driver, data.name):
        raise ValueError(f"driver name '{data.name}' already exists")
    driver = models.Driver(**data.model_dump())
    db.add(driver)
    db.commit()
    db.refresh(driver)
    return driver

def create_trip(db: Session, data: schemas.TripCreate) -> models.Trip:
    if not (data.rider_id or data.rider_name):
        raise ValueError("rider_id or rider_name required")
    if data.rider_id is not None and data.rider_name is not None:
        rider_by_id = _resolve_single(db, models.Rider, "rider", obj_id=data.rider_id)
        rider_by_name = _resolve_single(db, models.Rider, "rider", obj_name=data.rider_name)
        if rider_by_id.id != rider_by_name.id:
            raise ValueError(
                f"rider_id {data.rider_id} refers to '{rider_by_id.name}', "
                f"but rider_name '{data.rider_name}' has id {rider_by_name.id}"
            )
        rider = rider_by_id
    elif data.rider_id is not None:
        rider = _resolve_single(db, models.Rider, "rider", obj_id=data.rider_id)
    else:
        rider = _resolve_single(db, models.Rider, "rider", obj_name=data.rider_name)
    trip = models.Trip(rider_id=rider.id, origin=data.origin, dest=data.dest)
    db.add(trip)
    db.commit()
    db.refresh(trip)
    return trip

def list_riders(db: Session):
    return db.scalars(select(models.Rider)).all()

def list_drivers(db: Session):
    return db.scalars(select(models.Driver)).all()

def list_trips(db: Session):
    return db.scalars(select(models.Trip)).all()

def delete_rider(db: Session, rider_id=None, rider_name=None):
    rider = _resolve_single(db, models.Rider, "rider", obj_id=rider_id, obj_name=rider_name)
    db.delete(rider)
    db.commit()

def delete_driver(db: Session, driver_id=None, driver_name=None):
    driver = _resolve_single(db, models.Driver, "driver", obj_id=driver_id, obj_name=driver_name)
    db.delete(driver)
    db.commit()

def delete_trip(db: Session, trip_id: int):
    trip = _resolve_single(db, models.Trip, "trip", obj_id=trip_id)
    db.delete(trip)
    db.commit()

def update_status(db: Session, trip_id: int, status: models.TripStatus):
    trip = _resolve_single(db, models.Trip, "trip", obj_id=trip_id)
    trip.status = status
    db.commit()
    db.refresh(trip)
    return trip

def assign_driver(db: Session, trip_id: int, driver_id=None, driver_name=None):
    trip = _resolve_single(db, models.Trip, "trip", obj_id=trip_id)
    if trip.status != models.TripStatus.REQUESTED:
        raise ValueError("trip is already assigned or finished")
    if driver_id is not None and driver_name is not None:
        by_id = _resolve_single(db, models.Driver, "driver", obj_id=driver_id)
        by_name = _resolve_single(db, models.Driver, "driver", obj_name=driver_name)
        if by_id.id != by_name.id:
            raise ValueError(
                f"driver_id {driver_id} refers to '{by_id.name}', "
                f"but driver_name '{driver_name}' has id {by_name.id}"
            )
        driver = by_id
    elif driver_id is not None:
        driver = _resolve_single(db, models.Driver, "driver", obj_id=driver_id)
    else:
        driver = _resolve_single(db, models.Driver, "driver", obj_name=driver_name)
    trip.driver_id = driver.id
    trip.status = models.TripStatus.ACCEPTED
    db.commit()
    db.refresh(trip)
    return trip

def ping_trip(db: Session, trip_id: int) -> models.Trip:
    trip = _resolve_single(db, models.Trip, "trip", obj_id=trip_id)
    if trip.status in {models.TripStatus.ACCEPTED, models.TripStatus.STARTED}:
        if trip.status == models.TripStatus.ACCEPTED:
            trip.status = models.TripStatus.STARTED
        trip.remaining_km = max(0, trip.remaining_km - random.uniform(0.5, 2.0))
        if trip.remaining_km == 0:
            trip.status = models.TripStatus.ENDED
    db.commit()
    db.refresh(trip)
    return trip

def reset_database(db: Session):
    db.execute(delete(models.Trip))
    db.execute(delete(models.Driver))
    db.execute(delete(models.Rider))
    db.commit()