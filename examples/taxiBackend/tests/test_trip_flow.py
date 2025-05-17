from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_flow():
    rider_id = client.post("/riders", json={"name": "Alice"}).json()["id"]
    driver_id = client.post("/drivers", json={"name": "Bob", "car_plate": "XYZ"}).json()["id"]
    trip_id = client.post("/trips", json={"rider_id": rider_id, "origin": "A", "dest": "B"}).json()["id"]
    r = client.post(f"/trips/{trip_id}/assign", params={"driver_id": driver_id})
    assert r.status_code == 200
    data = client.post(f"/trips/{trip_id}/ping").json()
    assert data["remaining_km"] <= 10
    while not data["complete"]:
        data = client.post(f"/trips/{trip_id}/ping").json()
    assert data["complete"] is True
