from pydantic import BaseModel, ConfigDict
from typing import Optional
from .models import TripStatus


class RiderCreate(BaseModel):
    name: str


class DriverCreate(BaseModel):
    name: str
    car_plate: str


class RiderUpdate(BaseModel):
    name: Optional[str] = None


class DriverUpdate(BaseModel):
    name: Optional[str] = None
    car_plate: Optional[str] = None


class TripUpdate(BaseModel):
    origin: Optional[str] = None
    dest: Optional[str] = None
    fare: Optional[float] = None
    remaining_km: Optional[float] = None
    status: Optional[TripStatus] = None


class RiderOut(BaseModel):
    id: int
    name: str
    model_config = ConfigDict(from_attributes=True)


class DriverOut(BaseModel):
    id: int
    name: str
    car_plate: str
    model_config = ConfigDict(from_attributes=True)


class TripCreate(BaseModel):
    rider_id: Optional[int] = None
    rider_name: Optional[str] = None
    origin: str
    dest: str


class TripOut(BaseModel):
    id: int
    rider_id: int
    driver_id: Optional[int]
    origin: str
    dest: str
    fare: float
    remaining_km: float
    status: TripStatus
    model_config = ConfigDict(from_attributes=True)


class TripProgress(BaseModel):
    id: int
    remaining_km: float
    status: TripStatus
    complete: bool