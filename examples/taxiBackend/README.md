# Tiny Taxi Backend

A minimal FastAPI + SQLite taxi-hailing backend with simulated vehicle progress. Fully containerized, just add Docker.

## Quick Start

```bash
docker build -t taxi-backend .
docker run --rm -p 7788:7788 taxi-backend
```

- Swagger UI: http://localhost:7788/docs
- Health: http://localhost:7788/health
- Dashboard snapshot: http://localhost:7788/dashboard

## Example Play-Through

### Manual mode

```bash
# create rider and driver
rider_id=$(curl -s -X POST localhost:7788/riders  -H "Content-Type: application/json" -d '{"name":"Alice"}' | jq -r '.id')
driver_id=$(curl -s -X POST localhost:7788/drivers -H "Content-Type: application/json" -d '{"name":"Bob","car_plate":"XYZ"}' | jq -r '.id')

# request a trip
trip_id=$(curl -s -X POST localhost:7788/trips   -H "Content-Type: application/json" -d "{\"rider_id\":$rider_id,\"origin\":\"A\",\"dest\":\"B\"}" | jq -r '.id')

# assign the driver
curl -X POST "localhost:7788/trips/$trip_id/assign?driver_id=$driver_id"
```

```bash
# ping repeatedly until "complete": true
curl -X POST localhost:7788/trips/$trip_id/ping
```

### Three-minute auto mode

```bash
# 1. Create rider and driver
rider_id=$(curl -s -X POST localhost:7788/riders \
     -H "Content-Type: application/json" \
     -d '{"name":"Carol"}' | jq -r '.id')
driver_id=$(curl -s -X POST localhost:7788/drivers \
     -H "Content-Type: application/json" \
     -d '{"name":"Dave","car_plate":"ABC123"}' | jq -r '.id')

# 2. Request a trip
trip_id=$(curl -s -X POST localhost:7788/trips \
     -H "Content-Type: application/json" \
     -d "{\"rider_id\":$rider_id,\"origin\":\"C\",\"dest\":\"D\"}" | jq -r '.id')

# 3. Assign the driver
curl -X POST "localhost:7788/trips/$trip_id/assign?driver_id=$driver_id"

# 4. Auto-progress for 60 seconds and show final status
curl -X POST "localhost:7788/trips/$trip_id/simulate?duration=180"
```

## Run Tests

```bash
docker run --rm taxi-backend pytest
```

## Endpoints

| Method | Path                        | Description                                             |
|--------|-----------------------------|---------------------------------------------------------|
| POST   | `/riders`                   | Create rider                                            |
| GET    | `/riders`                   | List riders                                             |
| POST   | `/drivers`                  | Create driver                                           |
| GET    | `/drivers`                  | List drivers                                            |
| POST   | `/trips`                    | Create trip                                             |
| GET    | `/trips`                    | List trips                                              |
| GET    | `/trips/{trip_id}`          | Trip details                                            |
| POST   | `/trips/{trip_id}/assign`   | Assign or auto-assign driver                            |
| POST   | `/trips/{trip_id}/{action}` | Manually update status (`accepted`, `started`, `ended`) |
| POST   | `/trips/{trip_id}/ping`     | Single progress tick (subtract 0.5 – 2 km)              |
| POST   | `/trips/{trip_id}/simulate` | Auto-progress for given duration (seconds)              |
| GET    | `/dashboard`                | Snapshot of all data                                    |
| GET    | `/health`                   | Health check                                            |