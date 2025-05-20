import re
from build.nlip_models import NLIPRequest, AllowedFormat
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from . import crud, schemas
from .deps import get_db

router = APIRouter()


def make_reply(text: str) -> NLIPRequest:
    return NLIPRequest(
        messagetype=None,
        format=AllowedFormat.text,
        subformat="English",
        content=text,
        label=None,
        submessages=None,
    )


@router.post("/nlip", response_model=NLIPRequest)
@router.post("/nlip/", response_model=NLIPRequest)
def nlip(req: NLIPRequest, db: Session = Depends(get_db)) -> NLIPRequest:
    msg = req.content.strip().lower()

    if msg == "list riders":
        riders = crud.list_riders(db)
        return make_reply(
            "\n".join(f"{r.id}. {r.name}" for r in riders) or "0 riders"
        )

    if m := re.fullmatch(r"create rider (.+)", msg):
        rider = crud.create_rider(db, schemas.RiderCreate(name=m[1].title()))
        return make_reply(f"rider {rider.id} created: {rider.name}")

    if msg == "list drivers":
        drivers = crud.list_drivers(db)
        return make_reply(
            "\n".join(f"{d.id}. {d.name} ({d.car_plate})" for d in drivers) or "0 drivers"
        )

    if m := re.fullmatch(r"create driver (.+) plate ([a-z0-9]+)", msg):
        driver = crud.create_driver(
            db,
            schemas.DriverCreate(name=m[1].title(), car_plate=m[2].upper()),
        )
        return make_reply(f"driver {driver.id} created: {driver.name}")

    if m := re.fullmatch(r"request trip rider (\d+) from (.+) to (.+)", msg):
        trip = crud.create_trip(
            db,
            schemas.TripCreate(
                rider_id=int(m[1]),
                origin=m[2].upper(),
                dest=m[3].upper(),
            ),
        )
        return make_reply(f"trip {trip.id} requested (rider {trip.rider_id})")

    if m := re.fullmatch(r"assign driver (\d+) to trip (\d+)", msg):
        trip = crud.assign_driver(db, int(m[2]), int(m[1]))
        return make_reply(f"driver {trip.driver_id} assigned to trip {trip.id}")

    if m := re.fullmatch(r"ping trip (\d+)", msg):
        trip = crud.ping_trip(db, int(m[1]))
        return make_reply(f"{trip.remaining_km:.1f} km left — status {trip.status.value}")

    return make_reply("command not recognised")
