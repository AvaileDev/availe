import json
import os
import time
import threading
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from .ollama_client import chat_complete
from . import crud, schemas, models
from .deps import get_db
from .database import SessionLocal
from build.nlip_models import NLIPRequest, AllowedFormat

router = APIRouter()
TOOL_CALL_TOKEN = "<|tool_call|>"
SYS_PROMPT = open(os.path.join(os.path.dirname(__file__), "sys_prompt.txt"), encoding="utf-8").read()
tools = [
    {"type":"function","function":{"name":"list_riders","parameters":{"type":"object","properties":{}}}},
    {"type":"function","function":{"name":"create_rider","parameters":{"type":"object","properties":{"name":{"type":"string"}},"required":["name"]}}},
    {"type":"function","function":{"name":"delete_rider","parameters":{"type":"object","properties":{"rider_id":{"type":["integer","null"]},"rider_name":{"type":["string","null"]}}}}},
    {"type":"function","function":{"name":"list_drivers","parameters":{"type":"object","properties":{}}}},
    {"type":"function","function":{"name":"create_driver","parameters":{"type":"object","properties":{"name":{"type":"string"},"car_plate":{"type":"string"}},"required":["name","car_plate"]}}},
    {"type":"function","function":{"name":"delete_driver","parameters":{"type":"object","properties":{"driver_id":{"type":["integer","null"]},"driver_name":{"type":["string","null"]}}}}},
    {"type":"function","function":{"name":"list_trips","parameters":{"type":"object","properties":{}}}},
    {"type":"function","function":{"name":"request_trip","parameters":{"type":"object","properties":{"rider_id":{"type":["integer","null"]},"rider_name":{"type":["string","null"]},"origin":{"type":"string"},"dest":{"type":"string"},"duration":{"type":["integer","null"]}},"required":["origin","dest"]}}},
    {"type":"function","function":{"name":"assign_driver","parameters":{"type":"object","properties":{"trip_id":{"type":"integer"},"driver_id":{"type":["integer","null"]},"driver_name":{"type":["string","null"]}},"required":["trip_id"]}}},
    {"type":"function","function":{"name":"ping_trip","parameters":{"type":"object","properties":{"trip_id":{"type":"integer"}},"required":["trip_id"]}}},
    {"type":"function","function":{"name":"simulate_trip","parameters":{"type":"object","properties":{"trip_id":{"type":"integer"},"duration":{"type":["integer","null"]}},"required":["trip_id"]}}},
    {"type":"function","function":{"name":"reset_database","parameters":{"type":"object","properties":{}}}}
]
CALLABLES = {t["function"]["name"] for t in tools}
SIMULATIONS: dict[int,int] = {}

def _fmt_driver(d):
    return f"{d.id}\t{d.name}\t{d.car_plate}"

def _sanitize_id_and_name(clean: dict, id_key: str, name_key: str):
    if clean.get(name_key):
        clean[id_key] = None
    else:
        val = clean.get(id_key)
        if isinstance(val, str):
            clean[id_key] = int(val) if val.isdigit() else None
    return clean

def _sanitize_request_trip_args(args: dict) -> dict:
    clean = dict(args)
    dur = clean.get("duration")
    if isinstance(dur, str) and dur.strip().isdigit():
        clean["duration"] = int(dur.strip())
    rid = clean.get("rider_id")
    if isinstance(rid, str) and not rid.isdigit():
        clean["rider_name"] = rid
        clean["rider_id"] = None
    return _sanitize_id_and_name(clean, "rider_id", "rider_name")

def _sanitize_assign_driver_args(args: dict) -> dict:
    return _sanitize_id_and_name(dict(args), "driver_id", "driver_name")

def _sanitize_simulate_trip_args(args: dict) -> dict:
    clean = dict(args)
    tid = clean.get("trip_id")
    if isinstance(tid, str) and tid.isdigit():
        clean["trip_id"] = int(tid)
    dur = clean.get("duration")
    if isinstance(dur, str) and dur.strip().isdigit():
        clean["duration"] = int(dur.strip())
    return clean

def _simulate(db: Session, trip_id: int, duration: int) -> str:
    if duration <= 0:
        raise ValueError("duration must be positive")
    trip = db.get(models.Trip, trip_id)
    if not trip:
        raise ValueError("trip not found")
    if trip.status == models.TripStatus.REQUESTED:
        trip.status = models.TripStatus.STARTED
        db.commit()
        db.refresh(trip)
    if trip.status == models.TripStatus.ACCEPTED:
        trip.status = models.TripStatus.STARTED
        db.commit()
        db.refresh(trip)
    step_km = trip.remaining_km / duration if duration else trip.remaining_km
    end_at = time.time() + duration
    while time.time() < end_at and trip.remaining_km > 0:
        trip.remaining_km = max(0.0, trip.remaining_km - step_km)
        if trip.remaining_km == 0 and trip.status != models.TripStatus.ENDED:
            trip.status = models.TripStatus.ENDED
        db.commit()
        time.sleep(1)
    if trip.remaining_km > 0:
        trip.remaining_km = 0
        trip.status = models.TripStatus.ENDED
        db.commit()
    db.refresh(trip)
    return f"trip {trip.id} simulated for {duration}s — status {trip.status.value}"

def _background_simulate(trip_id: int, duration: int):
    db2 = SessionLocal()
    try:
        _simulate(db2, trip_id, duration)
    finally:
        db2.close()

def _run(action: str, args: dict, db: Session) -> str | None:
    match action:
        case "list_riders":
            riders = crud.list_riders(db)
            if not riders:
                return "0 riders"
            header = "id\tname"
            body = "\n".join(f"{r.id}\t{r.name}" for r in riders)
            return f"{header}\n{body}"
        case "create_rider":
            r = crud.create_rider(db, schemas.RiderCreate(**args))
            return f"rider {r.id} created: {r.name}"
        case "delete_rider":
            crud.delete_rider(db, args.get("rider_id"), args.get("rider_name"))
            return "rider deleted"
        case "list_drivers":
            drivers = crud.list_drivers(db)
            if not drivers:
                return "0 drivers"
            header = "id\tname\tplate"
            body = "\n".join(_fmt_driver(d) for d in drivers)
            return f"{header}\n{body}"
        case "create_driver":
            d = crud.create_driver(db, schemas.DriverCreate(**args))
            return f"driver {d.id} created: {d.name}"
        case "delete_driver":
            crud.delete_driver(db, args.get("driver_id"), args.get("driver_name"))
            return "driver deleted"
        case "list_trips":
            trips = crud.list_trips(db)
            if not trips:
                return "0 trips"
            header = "id\trider\tdest\tstatus"
            body = "\n".join(f"{t.id}\t{t.rider_id}\t{t.dest}\t{t.status.value}" for t in trips)
            return f"{header}\n{body}"
        case "request_trip":
            args = _sanitize_request_trip_args(args)
            duration = args.pop("duration", None)
            trip_kwargs = {k: v for k, v in args.items() if k in {"rider_id","rider_name","origin","dest"}}
            t = crud.create_trip(db, schemas.TripCreate(**trip_kwargs))
            if duration is not None:
                SIMULATIONS[t.id] = duration
            return f"trip {t.id} requested for rider {t.rider_id}"
        case "assign_driver":
            args = _sanitize_assign_driver_args(args)
            t = crud.assign_driver(db, **args)
            dur = SIMULATIONS.pop(t.id, None)
            if dur is not None:
                threading.Thread(target=_background_simulate, args=(t.id, dur), daemon=True).start()
                return f"driver {t.driver_id} assigned to trip {t.id}; simulation started for {dur}s"
            return f"driver {t.driver_id} assigned to trip {t.id}"
        case "ping_trip":
            t = crud.ping_trip(db, args["trip_id"])
            return f"{t.remaining_km:.1f} km left — status {t.status.value}"
        case "simulate_trip":
            args = _sanitize_simulate_trip_args(args)
            duration = args.get("duration", 60)
            return _simulate(db, args["trip_id"], duration)
        case "reset_database":
            crud.reset_database(db)
            return "database reset"
    return None


def _call_llm(history, tools=None):
    return chat_complete(messages=history, tools=tools)["choices"][0]["message"]

def _collect_calls(msg) -> list[dict]:
    calls = []
    if msg.get("tool_calls"):
        for tc in msg["tool_calls"]:
            fn = tc["function"]
            args = fn.get("arguments", {})
            if isinstance(args, str):
                try:
                    args = json.loads(args)
                except json.JSONDecodeError:
                    args = {}
            calls.append({"name": fn["name"], "arguments": args})
    else:
        text = (msg.get("content") or "").lstrip()
        if text.startswith(TOOL_CALL_TOKEN):
            calls = json.loads(text[len(TOOL_CALL_TOKEN):])
    return calls

@router.post("/nlip", response_model=NLIPRequest)
@router.post("/nlip/", response_model=NLIPRequest)
def nlip(req: NLIPRequest, db: Session = Depends(get_db)) -> NLIPRequest:
    history = [
        {"role": "system", "content": SYS_PROMPT},
        {"role": "available_tools", "content": json.dumps(tools)},
        {"role": "user", "content": req.content}
    ]
    msg = _call_llm(history, tools)
    calls = _collect_calls(msg)
    results = []
    for call in calls:
        name = call["name"]
        args = call.get("arguments", {})
        if isinstance(args, str):
            args = json.loads(args)
        try:
            res = _run(name, args, db)
        except Exception as e:
            res = str(e)
        results.append(res)
    if calls:
        content = TOOL_CALL_TOKEN + json.dumps(calls) + "\n\n" + "\n".join(results)
    else:
        content = TOOL_CALL_TOKEN + "[]" + "\n\n" + msg.get("content", "")
    return NLIPRequest(format=AllowedFormat.text, subformat="English", content=content)