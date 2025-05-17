import enum
from sqlalchemy import Column, Integer, String, Float, ForeignKey, Enum
from sqlalchemy.orm import relationship
from .database import Base

class TripStatus(str, enum.Enum):
    REQUESTED = "requested"
    ACCEPTED = "accepted"
    STARTED = "started"
    ENDED = "ended"

class Rider(Base):
    __tablename__ = "riders"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)

class Driver(Base):
    __tablename__ = "drivers"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    car_plate = Column(String, nullable=False)

class Trip(Base):
    __tablename__ = "trips"
    id = Column(Integer, primary_key=True, index=True)
    rider_id = Column(Integer, ForeignKey("riders.id"), nullable=False)
    driver_id = Column(Integer, ForeignKey("drivers.id"), nullable=True)
    origin = Column(String, nullable=False)
    dest = Column(String, nullable=False)
    fare = Column(Float, default=0)
    remaining_km = Column(Float, default=10)
    status = Column(Enum(TripStatus), default=TripStatus.REQUESTED)
    rider = relationship("Rider")
    driver = relationship("Driver")
