FROM python:3.12-slim AS base
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY app ./app
COPY tests ./tests
COPY gen_models.sh .
COPY nlip-api.yaml ./nlip-api.yaml
RUN bash gen_models.sh
ENV PYTHONPATH=/app
EXPOSE 7788
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "7788"]
