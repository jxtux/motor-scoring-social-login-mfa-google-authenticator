#!/usr/bin/env bash
set -euo pipefail
GROUP=${1:?Uso: $0 <group> <topic> <partition> <offset>}
TOPIC=${2:?}
PARTITION=${3:?}
OFFSET=${4:?}
docker exec motor-scoring-kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server kafka:29092 \
  --command-config /etc/kafka/client/admin.properties \
  --group "$GROUP" \
  --topic "${TOPIC}:${PARTITION}" \
  --reset-offsets --to-offset "$OFFSET" --execute
