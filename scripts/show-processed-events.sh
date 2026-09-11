#!/usr/bin/env bash
set -euo pipefail
docker exec -e PGPASSWORD="${POSTGRES_PASSWORD:-motor_scoring}" motor-scoring-postgres \
  psql -U "${POSTGRES_USER:-motor_scoring}" -d "${POSTGRES_DB:-motor_scoring}" \
  -c "SELECT event_id,consumer_name,topic,kafka_partition,kafka_offset,processed_at FROM processed_event ORDER BY processed_at DESC LIMIT 20;"
