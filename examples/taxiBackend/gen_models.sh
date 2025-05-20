#!/usr/bin/env bash
set -euo pipefail

SPEC_ROOT="nlip-api.yaml"
OUT_DIR="build"
OUT_FILE="${OUT_DIR}/nlip_models.py"

mkdir -p "${OUT_DIR}"

python -m datamodel_code_generator \
  --input             "${SPEC_ROOT}" \
  --input-file-type   openapi \
  --output            "${OUT_FILE}" \
  --base-class        pydantic.BaseModel \
  --target-python-version 3.12 \
  --field-constraints \
  --use-standard-collections

echo "Generated ${OUT_FILE}"
