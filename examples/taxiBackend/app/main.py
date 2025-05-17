from fastapi import FastAPI, Depends, HTTPException, Request
from fastapi.templating import Jinja2Templates
from fastapi.responses import HTMLResponse
from fastapi.staticfiles import StaticFiles
from sqlalchemy.orm import Session
from .database import Base, engine
from .deps import get_db
from . import crud, schemas, models
import time

Base.metadata.create_all(bind=engine)
app = FastAPI(title="Tiny Taxi Backend")

app.mount("/static", StaticFiles(directory="app/static"), name="static")

templates = Jinja2Templates(directory="app/templates")


@app.post("/riders", response_model=schemas.RiderOut, status_code=201)
def create_rider(payload: schemas.RiderCreate, db: Session = Depends(get_db)):
    return crud.create_rider(db, payload)


@app.get("/riders", response_model=list[schemas.RiderOut])
def riders(db: Session = Depends(get_db)):
    return crud.list_riders(db)


@app.post("/drivers", response_model=schemas.DriverOut, status_code=201)
def create_driver(payload: schemas.DriverCreate, db: Session = Depends(get_db)):
    return crud.create_driver(db, payload)


@app.get("/drivers", response_model=list[schemas.DriverOut])
def drivers(db: Session = Depends(get_db)):
    return crud.list_drivers(db)


@app.post("/trips", response_model=schemas.TripOut, status_code=201)
def request_trip(payload: schemas.TripCreate, db: Session = Depends(get_db)):
    return crud.create_trip(db, payload)


@app.get("/trips", response_model=list[schemas.TripOut])
def trips(db: Session = Depends(get_db)):
    return crud.list_trips(db)


@app.get("/trips/{trip_id}", response_model=schemas.TripOut)
def trip_detail(trip_id: int, db: Session = Depends(get_db)):
    trip = db.get(models.Trip, trip_id)
    if not trip:
        raise HTTPException(404, "trip not found")
    return trip


@app.post("/trips/{trip_id}/assign", response_model=schemas.TripOut)
def assign(trip_id: int, driver_id: int | None = None, db: Session = Depends(get_db)):
    try:
        return crud.assign_driver(db, trip_id, driver_id)
    except ValueError as e:
        raise HTTPException(400, str(e))


@app.post("/trips/{trip_id}/ping", response_model=schemas.TripProgress)
def ping(trip_id: int, db: Session = Depends(get_db)):
    trip = crud.ping_trip(db, trip_id)
    return schemas.TripProgress(
        id=trip.id,
        remaining_km=trip.remaining_km,
        status=trip.status,
        complete=trip.status == models.TripStatus.ENDED,
    )


@app.post("/trips/{trip_id}/simulate", response_model=schemas.TripProgress)
def simulate(trip_id: int, duration: int = 60, db: Session = Depends(get_db)):
    if duration <= 0:
        raise HTTPException(400, "duration must be positive")
    trip = db.get(models.Trip, trip_id)
    if not trip:
        raise HTTPException(404, "trip not found")
    if trip.status == models.TripStatus.REQUESTED:
        raise HTTPException(400, "trip has not been accepted yet; assign a driver first")
    if trip.status == models.TripStatus.ACCEPTED:
        trip.status = models.TripStatus.STARTED
        db.commit()
        db.refresh(trip)
    step_km = trip.remaining_km / duration
    end_at = time.time() + duration
    while time.time() < end_at:
        trip.remaining_km = max(0.0, trip.remaining_km - step_km)
        if trip.remaining_km == 0.0 and trip.status != models.TripStatus.ENDED:
            trip.status = models.TripStatus.ENDED
        db.commit()
        time.sleep(1)
    if trip.remaining_km > 0.0:
        trip.remaining_km = 0.0
        trip.status = models.TripStatus.ENDED
        db.commit()
    db.refresh(trip)
    return schemas.TripProgress(
        id=trip.id,
        remaining_km=trip.remaining_km,
        status=trip.status,
        complete=trip.status == models.TripStatus.ENDED,
    )


@app.post("/trips/{trip_id}/{action}", response_model=schemas.TripOut)
def change_status(trip_id: int, action: models.TripStatus, db: Session = Depends(get_db)):
    valid = {models.TripStatus.ACCEPTED, models.TripStatus.STARTED, models.TripStatus.ENDED}
    if action not in valid:
        raise HTTPException(400, "invalid action")
    return crud.update_status(db, trip_id, action)


@app.get("/dashboard.json")
def dashboard_json(db: Session = Depends(get_db)):
    return {
        "riders": crud.list_riders(db),
        "drivers": crud.list_drivers(db),
        "trips": crud.list_trips(db),
    }


@app.get("/dashboard", response_class=HTMLResponse)
def dashboard(request: Request):
    return templates.TemplateResponse("dashboard.html", {"request": request})


@app.get("/health")
def health():
    return {"status": "ok"}