#!/usr/bin/env bash
set -euo pipefail
CLIENT=/tmp/admin.properties
cat > "$CLIENT" <<PROPS
security.protocol=SASL_SSL
sasl.mechanism=SCRAM-SHA-512
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="admin" password="${KAFKA_ADMIN_PASSWORD}";
ssl.truststore.location=/etc/kafka/secrets/kafka.truststore.jks
ssl.truststore.password=${KAFKA_SSL_TRUSTSTORE_PASSWORD}
ssl.truststore.type=JKS
ssl.endpoint.identification.algorithm=https
PROPS

BOOTSTRAP=kafka:29092
until /opt/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server "$BOOTSTRAP" --command-config "$CLIENT" >/dev/null 2>&1; do
  echo "Waiting for secured Kafka..."; sleep 2
done

topics=(
  scoring.requested.v1 payment.validated.v1 payment.rejected.v1 scoring.calculated.v1 credit-score.delivered.v1
  scoring.requested.v1.DLT payment.validated.v1.DLT scoring.calculated.v1.DLT
)
for topic in "${topics[@]}"; do
  /opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP" --command-config "$CLIENT" \
    --create --if-not-exists --topic "$topic" --partitions 3 --replication-factor 1
done

acl_topic() {
  local principal=$1 topic=$2; shift 2
  /opt/kafka/bin/kafka-acls.sh --bootstrap-server "$BOOTSTRAP" --command-config "$CLIENT" --add \
    --allow-principal "User:${principal}" --topic "$topic" "$@"
}
acl_group() {
  local principal=$1 group=$2; shift 2
  /opt/kafka/bin/kafka-acls.sh --bootstrap-server "$BOOTSTRAP" --command-config "$CLIENT" --add \
    --allow-principal "User:${principal}" --group "$group" "$@"
}

# Outbox Publisher: publica los eventos de integración normales.
for t in scoring.requested.v1 payment.validated.v1 payment.rejected.v1 scoring.calculated.v1 credit-score.delivered.v1; do
  acl_topic outbox-publisher "$t" --operation Write --operation Describe
 done

# Consumers: leen su topic/grupo y pueden publicar únicamente en su DLT técnico.
acl_topic payment-validation scoring.requested.v1 --operation Read --operation Describe
acl_topic payment-validation scoring.requested.v1.DLT --operation Write --operation Describe
acl_group payment-validation payment-validation-group --operation Read --operation Describe

acl_topic scoring-calculation payment.validated.v1 --operation Read --operation Describe
acl_topic scoring-calculation payment.validated.v1.DLT --operation Write --operation Describe
acl_group scoring-calculation scoring-calculation-group --operation Read --operation Describe

acl_topic credit-score-delivery scoring.calculated.v1 --operation Read --operation Describe
acl_topic credit-score-delivery scoring.calculated.v1.DLT --operation Write --operation Describe
acl_group credit-score-delivery credit-score-delivery-group --operation Read --operation Describe

# DLT reprocessor: lectura de DLT y republicación al topic original.
for t in scoring.requested.v1.DLT payment.validated.v1.DLT scoring.calculated.v1.DLT; do
  acl_topic dlt-reprocessor "$t" --operation Read --operation Describe
 done
for t in scoring.requested.v1 payment.validated.v1 scoring.calculated.v1; do
  acl_topic dlt-reprocessor "$t" --operation Write --operation Describe
 done
/opt/kafka/bin/kafka-acls.sh --bootstrap-server "$BOOTSTRAP" --command-config "$CLIENT" --add \
  --allow-principal User:dlt-reprocessor --resource-pattern-type prefixed --group dlt-reprocessor- --operation Read --operation Describe

# Kafka UI para observabilidad de laboratorio.
/opt/kafka/bin/kafka-acls.sh --bootstrap-server "$BOOTSTRAP" --command-config "$CLIENT" --add \
  --allow-principal User:kafka-ui --topic '*' --operation Read --operation Describe
/opt/kafka/bin/kafka-acls.sh --bootstrap-server "$BOOTSTRAP" --command-config "$CLIENT" --add \
  --allow-principal User:kafka-ui --group '*' --operation Read --operation Describe
/opt/kafka/bin/kafka-acls.sh --bootstrap-server "$BOOTSTRAP" --command-config "$CLIENT" --add \
  --allow-principal User:kafka-ui --cluster --operation Describe --operation DescribeConfigs

echo "Kafka topics and ACLs initialized."
