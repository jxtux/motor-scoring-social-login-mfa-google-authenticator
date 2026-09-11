#!/usr/bin/env bash
set -euo pipefail
docker exec -e PGPASSWORD="${POSTGRES_PASSWORD:-motor_scoring}" motor-scoring-postgres \
  psql -U "${POSTGRES_USER:-motor_scoring}" -d "${POSTGRES_DB:-motor_scoring}" \
  -c "SELECT event_id,event_type,topic,event_key,status,created_at,published_at FROM outbox_event ORDER BY created_at DESC LIMIT 20;"
