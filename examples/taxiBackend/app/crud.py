from sqlalchemy.orm import Session
from sqlalchemy import select
import random
from . import models, schemas

def create_rider(db: Session, data: schemas.RiderCreate) -> models.Rider:
    rider = models.Rider(**data.model_dump())
    db.add(rider)
    db.commit()
    db.refresh(rider)
    return rider

def create_driver(db: Session, data: schemas.DriverCreate) -> models.Driver:
    driver = models.Driver(**data.model_dump())
    db.add(driver)
    db.commit()
    db.refresh(driver)
    return driver

def list_riders(db: Session):
    return db.scalars(select(models.Rider)).all()

def list_drivers(db: Session):
    return db.scalars(select(models.Driver)).all()

def list_trips(db: Session):
    return db.scalars(select(models.Trip)).all()

def create_trip(db: Session, data: schemas.TripCreate) -> models.Trip:
    trip = models.Trip(**data.model_dump())
    db.add(trip)
    db.commit()
    db.refresh(trip)
    return trip

def update_status(db: Session, trip_id: int, status: models.TripStatus):
    trip = db.get(models.Trip, trip_id)
    trip.status = status
    db.commit()
    db.refresh(trip)
    return trip

def assign_driver(db: Session, trip_id: int, driver_id: int | None = None):
    trip = db.get(models.Trip, trip_id)
    if trip.status != models.TripStatus.REQUESTED:
        raise ValueError("trip is already assigned or finished")
    if driver_id is None:
        drivers = list_drivers(db)
        if not drivers:
            raise ValueError("no drivers")
        driver_id = random.choice(drivers).id
    trip.driver_id = driver_id
    trip.status = models.TripStatus.ACCEPTED
    db.commit()
    db.refresh(trip)
    return trip

def ping_trip(db: Session, trip_id: int) -> models.Trip:
    trip = db.get(models.Trip, trip_id)
    if trip.status in {models.TripStatus.ACCEPTED, models.TripStatus.STARTED}:
        if trip.status == models.TripStatus.ACCEPTED:
            trip.status = models.TripStatus.STARTED
        trip.remaining_km = max(0, trip.remaining_km - random.uniform(0.5, 2.0))
        if trip.remaining_km == 0:
            trip.status = models.TripStatus.ENDED
    db.commit()
    db.refresh(trip)
    return trip