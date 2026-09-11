#!/usr/bin/env bash
set -euo pipefail

CONFIG=/opt/kafka/config/motor-scoring-server.properties
DATA_DIR=/var/lib/kafka/data
mkdir -p "$DATA_DIR" /etc/kafka/client

cat > "$CONFIG" <<PROPS
process.roles=broker,controller
node.id=1
controller.quorum.voters=1@kafka:9093
controller.listener.names=CONTROLLER
listeners=SASL_SSL_INTERNAL://0.0.0.0:29092,SASL_SSL_EXTERNAL://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
advertised.listeners=SASL_SSL_INTERNAL://kafka:29092,SASL_SSL_EXTERNAL://localhost:9092
listener.security.protocol.map=SASL_SSL_INTERNAL:SASL_SSL,SASL_SSL_EXTERNAL:SASL_SSL,CONTROLLER:SSL
inter.broker.listener.name=SASL_SSL_INTERNAL
early.start.listeners=CONTROLLER
log.dirs=$DATA_DIR
num.partitions=3
default.replication.factor=1
min.insync.replicas=1
offsets.topic.replication.factor=1
transaction.state.log.replication.factor=1
transaction.state.log.min.isr=1
group.initial.rebalance.delay.ms=0

ssl.keystore.location=/etc/kafka/secrets/kafka.keystore.jks
ssl.keystore.password=${KAFKA_SSL_KEYSTORE_PASSWORD}
ssl.key.password=${KAFKA_SSL_KEY_PASSWORD}
ssl.truststore.location=/etc/kafka/secrets/kafka.truststore.jks
ssl.truststore.password=${KAFKA_SSL_TRUSTSTORE_PASSWORD}
ssl.client.auth=none
listener.name.controller.ssl.client.auth=required

sasl.enabled.mechanisms=SCRAM-SHA-512
sasl.mechanism.inter.broker.protocol=SCRAM-SHA-512
listener.name.sasl_ssl_internal.scram-sha-512.sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="kafka" password="${KAFKA_BROKER_PASSWORD}";
listener.name.sasl_ssl_external.scram-sha-512.sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required;

authorizer.class.name=org.apache.kafka.metadata.authorizer.StandardAuthorizer
super.users=User:admin;User:kafka;User:CN=kafka
allow.everyone.if.no.acl.found=false
PROPS

cat > /etc/kafka/client/admin.properties <<PROPS
security.protocol=SASL_SSL
sasl.mechanism=SCRAM-SHA-512
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="admin" password="${KAFKA_ADMIN_PASSWORD}";
ssl.truststore.location=/etc/kafka/secrets/kafka.truststore.jks
ssl.truststore.password=${KAFKA_SSL_TRUSTSTORE_PASSWORD}
ssl.truststore.type=JKS
ssl.endpoint.identification.algorithm=https
PROPS

if [[ ! -f "$DATA_DIR/meta.properties" ]]; then
  echo "Formatting KRaft metadata and creating initial SCRAM credentials..."
  /opt/kafka/bin/kafka-storage.sh format --ignore-formatted \
    -t "${KAFKA_CLUSTER_ID}" -c "$CONFIG" \
    --add-scram "SCRAM-SHA-512=[name=admin,password=${KAFKA_ADMIN_PASSWORD}]" \
    --add-scram "SCRAM-SHA-512=[name=kafka,password=${KAFKA_BROKER_PASSWORD}]" \
    --add-scram "SCRAM-SHA-512=[name=motor-scoring-api,password=${KAFKA_API_PASSWORD}]" \
    --add-scram "SCRAM-SHA-512=[name=outbox-publisher,password=${KAFKA_OUTBOX_PASSWORD}]" \
    --add-scram "SCRAM-SHA-512=[name=payment-validation,password=${KAFKA_PAYMENT_PASSWORD}]" \
    --add-scram "SCRAM-SHA-512=[name=scoring-calculation,password=${KAFKA_SCORING_PASSWORD}]" \
    --add-scram "SCRAM-SHA-512=[name=credit-score-delivery,password=${KAFKA_DELIVERY_PASSWORD}]" \
    --add-scram "SCRAM-SHA-512=[name=dlt-reprocessor,password=${KAFKA_DLT_PASSWORD}]" \
    --add-scram "SCRAM-SHA-512=[name=kafka-ui,password=${KAFKA_UI_PASSWORD}]"
fi

exec /opt/kafka/bin/kafka-server-start.sh "$CONFIG"
