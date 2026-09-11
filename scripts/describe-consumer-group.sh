#!/usr/bin/env bash
set -euo pipefail
GROUP=${1:?Uso: $0 <consumer-group>}
docker exec motor-scoring-kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server kafka:29092 \
  --command-config /etc/kafka/client/admin.properties \
  --group "$GROUP" --describe
