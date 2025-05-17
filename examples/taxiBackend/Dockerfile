FROM python:3.12-slim AS base
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY app ./app
COPY tests ./tests
ENV PYTHONPATH=/app
EXPOSE 7788
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "7788"]
