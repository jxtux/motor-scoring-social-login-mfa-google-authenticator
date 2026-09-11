#!/usr/bin/env bash
set -euo pipefail
TOPIC=${1:?Uso: $0 <topic> [max-messages]}
MAX=${2:-20}
docker exec motor-scoring-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:29092 \
  --consumer.config /etc/kafka/client/admin.properties \
  --topic "$TOPIC" --from-beginning --max-messages "$MAX" \
  --property print.key=true \
  --property print.partition=true \
  --property print.offset=true \
  --property print.timestamp=true
