# Tiny Taxi Backend

A minimal FastAPI + SQLite taxi-hailing backend with simulated vehicle progress.

## Quick Start

```bash
poetry config virtualenvs.in-project true
poetry install
```

```bash
poetry run uvicorn app.main:app --host 0.0.0.0 --port 7788 --reload
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
poetry run pytest
```