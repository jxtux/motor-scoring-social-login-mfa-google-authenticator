#!/usr/bin/env bash
set -euo pipefail
TOPIC=${1:?Uso: $0 <dlt-topic> <event-id>}
EVENT_ID=${2:?}
DEMO_DLT_TOPIC="$TOPIC" DEMO_DLT_EVENT_ID="$EVENT_ID" \
  docker compose --profile tools run --rm dlt-reprocessor
