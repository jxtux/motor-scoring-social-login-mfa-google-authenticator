# README_PRUEBAS — Demostración práctica de Apache Kafka

Este documento es el **guion de pruebas y sustentación técnica** del proyecto. Cada prueba indica qué concepto de Kafka se demuestra, qué comandos ejecutar y qué resultado observar.

> Para arquitectura, instalación y composición del proyecto, consulta [`README.md`](README.md).

---

# 1. Preparación del laboratorio

## 1.1 Configurar variables

```bash
cp .env.example .env
```

Completa Gmail si deseas demostrar el envío real:

```text
GMAIL_USERNAME=tu-cuenta@gmail.com
GMAIL_APP_PASSWORD=tu-app-password
```

## 1.2 Generar TLS local

```bash
./scripts/generate-dev-certs.sh
```

## 1.3 Levantar todo

```bash
docker compose up -d --build
```

## 1.4 Estado

```bash
docker compose ps
```

Esperado:

```text
postgres                         UP / healthy
kafka                            UP / healthy
kafka-init                       exited 0
motor-scoring-api                UP / healthy
outbox-publisher                 UP
payment-consumer                 UP
scoring-consumer                 UP
credit-score-delivery-consumer   UP
kafka-ui                         UP
frontend-angular                 UP
```

## 1.5 Herramientas visuales

- Angular: `http://localhost:4200`
- Kafka UI: `http://localhost:8085`
- Swagger: `https://localhost:8443/swagger-ui.html`

---

# 2. PRUEBA — Kafka KRaft y nodo único

## Objetivo

Demostrar que Kafka está ejecutándose en **KRaft**, sin ZooKeeper.

## Pasos

```bash
docker compose ps kafka
```

Luego:

```bash
docker exec motor-scoring-kafka \
  /opt/kafka/bin/kafka-metadata-quorum.sh \
  --bootstrap-server kafka:29092 \
  --command-config /etc/kafka/client/admin.properties \
  describe --status
```

## Qué explicar

```text
1 Kafka Node
├── Broker
└── Controller
    └── KRaft
```

No se está demostrando alta disponibilidad de brokers. Es un laboratorio de un nodo.

---

# 3. PRUEBA — Topics y 3 partitions

## Objetivo

Demostrar Topic y Partition.

## Consola

```bash
docker exec motor-scoring-kafka \
  /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server kafka:29092 \
  --command-config /etc/kafka/client/admin.properties \
  --list
```

Describir un topic:

```bash
docker exec motor-scoring-kafka \
  /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server kafka:29092 \
  --command-config /etc/kafka/client/admin.properties \
  --describe --topic scoring.requested.v1
```

## Esperado

```text
PartitionCount: 3
ReplicationFactor: 1
P0
P1
P2
```

También puede mostrarse en Kafka UI.

## Conceptos

- Topic
- Partition
- Broker
- RF=1 en laboratorio

---

# 4. PRUEBA — Producer, key, partition y offset

## Objetivo

Mostrar los datos reales de un record Kafka.

## Paso 1

Crear una solicitud desde Angular o:

```bash
./scripts/send-demo-request.sh tu-correo@gmail.com 12345678 PAGO-DEMO-BCP-001
```

## Paso 2

```bash
./scripts/consume-topic.sh scoring.requested.v1 10
```

## Observar

```text
key       = solicitudScoringId
partition = 0, 1 o 2
offset    = posición dentro de esa partition
value     = EventEnvelope JSON
```

Ejemplo conceptual:

```text
Partition:1
Offset:12
Key:550e8400-e29b-41d4-a716-446655440000
Value:{"eventId":"...","eventType":"ScoringRequested",...}
```

## Explicación

La `key` no es el offset ni el `eventId`.

```text
solicitudScoringId → Kafka key → particionador → P0/P1/P2
offset             → posición del record dentro de esa partition
eventId            → identidad única del evento
```

---

# 5. PRUEBA — eventId, correlationId, causationId y key

## Objetivo

Trazar una solicitud completa.

Usa Kafka UI y busca el `solicitudScoringId`/key en:

```text
scoring.requested.v1
payment.validated.v1
scoring.calculated.v1
credit-score.delivered.v1
```

## Esperado

```text
ScoringRequested
 eventId=A
 correlationId=X
 causationId=null
 key=SOLICITUD-X

PaymentValidated
 eventId=B
 correlationId=X
 causationId=A
 key=SOLICITUD-X

ScoringCalculated
 eventId=C
 correlationId=X
 causationId=B
 key=SOLICITUD-X
```

## Conceptos

- `eventId`: cambia por evento.
- `correlationId`: permanece en todo el proceso.
- `causationId`: referencia al evento anterior.
- `key`: representa la solicitud para particionamiento.

---

# 6. PRUEBA — Producer y Consumer desacoplados

## Objetivo

Demostrar que el producer puede producir aunque el consumer esté apagado.

## Paso 1 — Detener Payment Consumer

```bash
docker compose stop payment-consumer
```

## Paso 2 — Generar solicitudes

Usa distintos DNI y pagos disponibles:

```bash
./scripts/send-demo-request.sh tu-correo@gmail.com 12345671 PAGO-DEMO-BCP-001
./scripts/send-demo-request.sh tu-correo@gmail.com 12345672 PAGO-DEMO-BCP-002
./scripts/send-demo-request.sh tu-correo@gmail.com 12345673 PAGO-DEMO-BCP-003
```

## Paso 3 — Mostrar records

```bash
./scripts/consume-topic.sh scoring.requested.v1 20
```

Los records están en Kafka aunque `payment-validation-group` no tenga consumer activo.

## Paso 4 — Levantar Consumer

```bash
docker compose start payment-consumer
```

## Paso 5 — Logs

```bash
docker compose logs -f payment-consumer
```

## Conceptos

- desacoplamiento;
- retention;
- backlog;
- offsets del Consumer Group.

---

# 7. PRUEBA — Consumer Lag

## Objetivo

Mostrar mensajes pendientes de un grupo.

Con `payment-consumer` detenido y solicitudes acumuladas:

```bash
./scripts/describe-consumer-group.sh payment-validation-group
```

Observar:

```text
CURRENT-OFFSET
LOG-END-OFFSET
LAG
```

Luego:

```bash
docker compose start payment-consumer
watch -n 1 ./scripts/describe-consumer-group.sh payment-validation-group
```

El `LAG` debe disminuir hasta 0.

---

# 8. PRUEBA — Consumer Group con 3 consumers y 3 partitions

## Objetivo

Demostrar paralelismo dentro del mismo group.

Por defecto cada container utiliza `KAFKA_CONCURRENCY=1`.

Escalar:

```bash
docker compose up -d --scale payment-consumer=3 payment-consumer
```

Ver containers:

```bash
docker compose ps payment-consumer
```

Ver group:

```bash
./scripts/describe-consumer-group.sh payment-validation-group
```

## Esperado

Conceptualmente:

```text
P0 → Consumer A
P1 → Consumer B
P2 → Consumer C
```

Con 3 partitions, un cuarto consumer del mismo group quedaría sin partition asignada.

---

# 9. PRUEBA — Rebalance

## Objetivo

Demostrar redistribución de partitions al desaparecer un consumer.

Primero escala 3:

```bash
docker compose up -d --scale payment-consumer=3 payment-consumer
```

Obtén IDs:

```bash
docker compose ps -q payment-consumer
```

Detén solo uno:

```bash
docker stop $(docker compose ps -q payment-consumer | head -n 1)
```

Luego:

```bash
./scripts/describe-consumer-group.sh payment-validation-group
```

## Esperado

Las 3 partitions se redistribuyen entre los consumers restantes.

## Recuperar 3 replicas

```bash
docker compose up -d --scale payment-consumer=3 payment-consumer
```

Kafka vuelve a ejecutar un rebalance.

---

# 10. PRUEBA — Flujo EDA end-to-end

## Objetivo

Mostrar la cadena completa.

Enviar solicitud válida y observar:

```text
ScoringRequested
    ↓
PaymentValidated
    ↓
ScoringCalculated
    ↓
CreditScoreDelivered
```

Consolas recomendadas:

```bash
docker compose logs -f outbox-publisher
```

```bash
docker compose logs -f payment-consumer scoring-consumer credit-score-delivery-consumer
```

Y Kafka UI en paralelo.

## Resultado final

- solicitud registrada con HTTP `202 Accepted`;
- score calculado por el Domain existente;
- Gmail recibe correo;
- PDF está adjunto;
- PDF no queda almacenado en PostgreSQL.

---

# 11. PRUEBA — Transactional Outbox

## Objetivo

Demostrar que registrar la solicitud y dejar el evento pendiente son una única transacción PostgreSQL.

## Paso 1 — Detener solo el publisher

```bash
docker compose stop outbox-publisher
```

La API sigue `UP`.

## Paso 2 — Registrar solicitud

```bash
./scripts/send-demo-request.sh tu-correo@gmail.com 12345674 PAGO-DEMO-BCP-004
```

## Paso 3 — Consultar Outbox

```bash
./scripts/show-outbox.sh
```

Esperado para el nuevo `ScoringRequested`:

```text
status=PENDING
published_at=NULL
```

La solicitud existe en `solicitudes_credito` y su workflow existe, pero ese `eventId` todavía no aparece en Kafka.

## Paso 4 — Levantar publisher

```bash
docker compose start outbox-publisher
```

## Paso 5

```bash
./scripts/show-outbox.sh
```

Esperado:

```text
status=PUBLISHED
published_at=<fecha>
```

Y el mismo `eventId` aparece en `scoring.requested.v1`.

## Concepto

```text
BD + Outbox = transacción local PostgreSQL
OutboxPublisher = publicación posterior a Kafka
```

---

# 12. PRUEBA — ACKs e idempotent producer

## Objetivo

Mostrar la configuración de confiabilidad del producer.

En `application.yml`:

```text
acks=all
enable.idempotence=true
```

Para una prueba operacional, detener Kafka mientras hay un evento Outbox pendiente:

```bash
docker compose stop kafka
```

Crear una solicitud. PostgreSQL puede guardar la solicitud y el Outbox queda `PENDING`; el publisher no puede obtener ACK.

Levantar Kafka:

```bash
docker compose start kafka
```

Al recuperarse el broker, el Outbox Publisher vuelve a publicar y marca `PUBLISHED` después del ACK.

> Con un solo broker y RF=1 esta prueba no demuestra replicación ni tolerancia a caída de broker. Demuestra confirmación del broker y recuperación de publicación.

---

# 13. PRUEBA — At-least-once mediante replay controlado

## Objetivo

Demostrar que un consumer debe tolerar que **el mismo record sea entregado más de una vez**.

La prueba usa un reset de offset porque es una forma determinista y fácil de exponer. En producción una redelivery también puede ocurrir por fallos/rebalances antes de confirmar el offset.

## Paso 1 — Procesar normalmente un `PaymentValidated`

Completa una solicitud válida y espera que `scoring-consumer` la procese.

## Paso 2 — Obtener el record procesado

```bash
./scripts/show-processed-events.sh
```

Busca una fila:

```text
consumer_name = scoring-calculation-group
topic         = payment.validated.v1
kafka_partition = 1      ← ejemplo
kafka_offset    = 12     ← ejemplo
event_id        = EVENT-X
```

También puedes consultar el group:

```bash
./scripts/describe-consumer-group.sh scoring-calculation-group
```

## Paso 3 — Detener el consumer group

```bash
docker compose stop scoring-consumer
```

El group debe estar inactivo para resetear el offset de forma controlada.

## Paso 4 — Retroceder al offset ya procesado

```bash
./scripts/reset-consumer-offset.sh \
  scoring-calculation-group \
  payment.validated.v1 \
  1 \
  12
```

Sustituye `1` y `12` por los valores reales de `processed_event`.

Equivale a ejecutar:

```bash
kafka-consumer-groups.sh \
  --group scoring-calculation-group \
  --topic payment.validated.v1:1 \
  --reset-offsets --to-offset 12 --execute
```

El offset pertenece a **Kafka**, no a PostgreSQL. PostgreSQL solo guarda una copia informativa en `processed_event` para trazabilidad/idempotencia.

## Paso 5 — Levantar nuevamente

```bash
docker compose start scoring-consumer
```

## Paso 6 — Ver logs

```bash
docker compose logs -f scoring-consumer
```

Kafka vuelve a entregar exactamente el mismo record:

```text
same topic
same partition
same offset
same eventId
```

Esto demuestra la posibilidad de **redelivery** que un diseño at-least-once debe tolerar.

---

# 14. PRUEBA — Idempotent Consumer

Esta prueba continúa inmediatamente la prueba anterior.

## Esperado

`processed_event` ya contiene:

```text
(eventId, scoring-calculation-group)
```

El consumer detecta el duplicado y escribe un log similar a:

```text
eventId=EVENT-X
consumerGroup=scoring-calculation-group
status=ALREADY_PROCESSED
action=SKIP
```

`ALREADY_PROCESSED` y `SKIP` son logs definidos por la aplicación para que la demostración sea clara; no son mensajes automáticos de Kafka.

## Verificación de no duplicidad

```bash
docker exec -e PGPASSWORD="${POSTGRES_PASSWORD:-motor_scoring}" motor-scoring-postgres \
  psql -U "${POSTGRES_USER:-motor_scoring}" -d "${POSTGRES_DB:-motor_scoring}" \
  -c "SELECT id_solicitud, COUNT(*) FROM evaluaciones_crediticias GROUP BY id_solicitud ORDER BY id_solicitud DESC LIMIT 10;"
```

Para la solicitud elegida debe existir una sola evaluación.

## Qué se demuestra

```text
Kafka entrega record otra vez      → at-least-once / replay
processed_event detecta eventId    → Idempotent Consumer
no duplica EvaluacionCrediticia    → efecto de negocio protegido
```

---

# 15. PRUEBA — Retry por fallo técnico

## Objetivo

Mostrar los reintentos configurados por Spring Kafka.

El error handler utiliza:

```text
intento inicial
+ 2 retries
backoff fijo de 2 segundos
→ DLT
```

La prueba más realista del proyecto se realiza con PostgreSQL caído en la siguiente sección.

---

# 16. PRUEBA — PostgreSQL DOWN → Retry → DLT

## Objetivo

Demostrar recuperación ante fallo de infraestructura sin confundirlo con el primer registro REST.

> PostgreSQL **no debe apagarse antes de crear la solicitud**, porque en ese caso no existiría ni solicitud ni Outbox. La caída se provoca cuando `PaymentValidated` ya está en Kafka.

## Paso 1 — Asegurar una sola instancia de scoring consumer

```bash
docker compose up -d --scale scoring-consumer=1 scoring-consumer
```

## Paso 2 — Pausar el proceso, NO detenerlo

```bash
docker compose pause scoring-consumer
```

Se usa `pause` porque el proceso Spring Boot ya inicializó JPA. Si se intentara iniciar desde cero mientras PostgreSQL está caído, Spring podría no completar su arranque y no llegaríamos al listener Kafka.

## Paso 3 — Enviar una solicitud con pago nuevo

```bash
./scripts/send-demo-request.sh tu-correo@gmail.com 12345675 PAGO-DEMO-BCP-005
```

Payment Consumer sigue funcionando y genera `PaymentValidated`.

## Paso 4 — Comprobar que `PaymentValidated` ya existe

```bash
./scripts/consume-topic.sh payment.validated.v1 50
```

Identifica el `eventId` de la solicitud que acabas de crear. Guárdalo para el reproceso posterior.

## Paso 5 — Apagar PostgreSQL

```bash
docker compose stop postgres
```

## Paso 6 — Reanudar Scoring Consumer

```bash
docker compose unpause scoring-consumer
```

## Paso 7 — Observar retries

```bash
docker compose logs -f scoring-consumer
```

El consumer intenta iniciar/ejecutar su transacción PostgreSQL, falla y el `DefaultErrorHandler` reintenta.

## Paso 8 — Ver DLT

```bash
./scripts/consume-topic.sh payment.validated.v1.DLT 50
```

O Kafka UI.

Debe existir el record con el mismo:

```text
key
eventId
correlationId
payload
```

Además Spring Kafka agrega headers técnicos del error/DLT.

## Conceptos

- Retry
- Backoff
- DLT
- Fallo técnico
- El stream normal no queda bloqueado indefinidamente

---

# 17. PRUEBA — Recuperar PostgreSQL y reprocesar DLT

## Paso 1 — Levantar PostgreSQL

```bash
docker compose start postgres
```

Esperar:

```bash
docker compose ps postgres
```

hasta `healthy`.

## Punto importante

El evento **no sale solo del DLT** al recuperar PostgreSQL.

DLT es un topic Kafka normal sujeto a retention.

## Paso 2 — Reprocesar controladamente

Con el `eventId` guardado en la prueba anterior:

```bash
./scripts/reprocess-dlt.sh payment.validated.v1.DLT EVENT-ID-AQUI
```

Internamente:

```text
payment.validated.v1.DLT
       ↓
DltReprocessor
       ↓ republish
payment.validated.v1
       ↓
scoring-calculation-group
       ↓
PostgreSQL disponible
       ↓
ScoringCalculated
```

## Importante

El record histórico **permanece en el DLT** hasta que retention lo elimine. Reprocesar significa republicar una copia al topic original; no mover físicamente el log record.

## Verificar

```bash
docker compose logs -f scoring-consumer outbox-publisher
```

Luego:

```bash
./scripts/consume-topic.sh scoring.calculated.v1 50
```

Finalmente debe continuar la entrega de correo.

---

# 18. PRUEBA — PaymentRejected es error de negocio, no DLT

## Objetivo

Diferenciar fallo técnico de rechazo de negocio.

Enviar una operación inexistente:

```bash
./scripts/send-demo-request.sh tu-correo@gmail.com 12345676 OPERACION-INEXISTENTE
```

## Esperado

```text
ScoringRequested
       ↓
Payment Consumer
       ↓
PaymentRejected
```

Ver:

```bash
./scripts/consume-topic.sh payment.rejected.v1 20
```

No debe pasar por retries/DLT porque el sistema funcionó correctamente; simplemente el pago no cumplió la regla de validación.

---

# 19. PRUEBA — Pago simulado no puede reutilizarse

Usa una operación válida una vez.

Después intenta otra solicitud con la misma operación.

El repositorio realiza un claim atómico equivalente a:

```text
UPDATE pago_simulado
SET utilizado=true
WHERE pago_simulado_id=?
  AND utilizado=false
```

Una sola solicitud obtiene el pago; las demás reciben `PaymentRejected`.

Esto también permite demostrar procesamiento concurrente con varios payment consumers.

---

# 20. PRUEBA — Seguridad SASL_SSL

## Objetivo

Demostrar que Kafka utiliza:

```text
TLS → cifrado
SASL/SCRAM-SHA-512 → autenticación
ACL → autorización
```

## TLS

Desde el host:

```bash
openssl s_client \
  -connect localhost:9092 \
  -CAfile backend-java/docker/certs/ca.crt \
  -verify_hostname localhost </dev/null
```

Debe completarse el handshake TLS y validarse el certificado local.

## Configuración Kafka

```bash
docker exec motor-scoring-kafka grep -E \
  'sasl.enabled.mechanisms|authorizer.class.name|listeners=' \
  /opt/kafka/config/motor-scoring-server.properties
```

Esperado:

```text
SASL_SSL
SCRAM-SHA-512
StandardAuthorizer
```

---

# 21. PRUEBA — ACL / mínimo privilegio

## Objetivo

Probar que un usuario autenticado no puede escribir en un topic no autorizado.

Ejemplo conceptual: `payment-validation` puede leer `scoring.requested.v1`, pero no debería escribir directamente `scoring.calculated.v1`.

Puedes listar ACL con admin:

```bash
docker exec motor-scoring-kafka \
  /opt/kafka/bin/kafka-acls.sh \
  --bootstrap-server kafka:29092 \
  --command-config /etc/kafka/client/admin.properties \
  --list
```

Para la exposición es suficiente mostrar los principals y los permisos de topic/group creados por `docker/kafka/init-topics-acls.sh`.

Una prueba negativa con credenciales restringidas debe terminar en un error de autorización (`TopicAuthorizationException`).

---

# 22. PRUEBA — PostgreSQL usa TLS

## Objetivo

Mostrar que la conexión del backend se configura con:

```text
sslmode=verify-full
```

Ver `application-postgres.yml` y el JDBC URL del container:

```bash
docker compose exec motor-scoring-api printenv POSTGRES_JDBC_URL
```

La imagen PostgreSQL arranca con `ssl=on` y rechaza conexiones `hostnossl` mediante `pg_hba.conf`.

---

# 23. PRUEBA — PDF solo en memoria + Gmail SMTP

## Objetivo

Demostrar que el flujo final no usa storage.

Ejecuta una solicitud válida y abre Gmail.

Esperado:

```text
correo real
subject: Resultado de su evaluación de score crediticio
PDF adjunto: score-crediticio-<solicitudScoringId>.pdf
```

El PDF contiene:

- datos del solicitante;
- score / 1000;
- resultado;
- producto;
- resumen financiero;
- capacidad de pago;
- relación deuda-ingreso;
- relación cuota-ingreso;
- factores y reglas;
- versión del modelo.

Verifica en PostgreSQL que no existe una tabla de PDFs ni una tabla de emails enviados.

---

# 24. PRUEBA — Flyway

## Objetivo

Demostrar versionado de la base de datos.

```bash
docker exec -e PGPASSWORD="${POSTGRES_PASSWORD:-motor_scoring}" motor-scoring-postgres \
  psql -U "${POSTGRES_USER:-motor_scoring}" -d "${POSTGRES_DB:-motor_scoring}" \
  -c "SELECT installed_rank,version,description,success FROM flyway_schema_history ORDER BY installed_rank;"
```

Esperado:

```text
V1 ...
V2 ...
V3 ...
V4 ...
V5 ...
```

---

# 25. PRUEBA — Clean Architecture / ArchUnit

## Objetivo

Demostrar automáticamente que el núcleo no depende de Kafka/Spring/JPA.

```bash
cd backend-java
mvn test
```

Entre los tests se encuentran reglas ArchUnit que verifican:

```text
Domain !→ Spring
Domain !→ Kafka
Domain !→ JPA
Application !→ Infrastructure
Presentation !→ Persistence/Kafka
```

---

# 26. PRUEBA — Tests automáticos

Ejecutar:

```bash
cd backend-java
mvn clean verify
```

Cobertura disponible/incluida:

- unit tests originales del Domain;
- unit tests originales de Application;
- ArchUnit;
- `IdempotentConsumerTest`;
- migraciones PostgreSQL reales con Testcontainers cuando Docker está disponible.

La demostración de Kafka distribuido se hace principalmente con Docker Compose + Kafka UI + CLI porque permite observar partitions, offsets, lag, rebalances, DLT y seguridad de manera visual.

---

# 27. PRUEBA — Consolas recomendadas durante la exposición

Mantén abiertas cuatro terminales:

## Terminal 1 — estado Docker

```bash
docker compose ps
```

## Terminal 2 — API + Outbox

```bash
docker compose logs -f motor-scoring-api outbox-publisher
```

## Terminal 3 — Consumers

```bash
docker compose logs -f payment-consumer scoring-consumer credit-score-delivery-consumer
```

## Terminal 4 — Kafka CLI

```bash
./scripts/consume-topic.sh scoring.calculated.v1 20
```

Y Kafka UI en el navegador.

---

# 28. Orden recomendado de la sustentación

```text
1. docker compose ps
2. Kafka UI: nodo KRaft
3. Topics + 3 partitions
4. Crear solicitud
5. Mostrar key / partition / offset
6. eventId / correlationId / causationId
7. Apagar Payment Consumer
8. Generar backlog
9. Mostrar consumer lag
10. Levantar Payment Consumer
11. Escalar 3 consumers
12. Mostrar assignment P0/P1/P2
13. Detener un member y mostrar rebalance
14. Flujo EDA completo
15. Detener OutboxPublisher
16. Mostrar PENDING
17. Levantar OutboxPublisher
18. Mostrar PUBLISHED
19. Replay por reset de offset
20. Mostrar ALREADY_PROCESSED / SKIP
21. Verificar evaluación no duplicada
22. Pausar Scoring Consumer
23. Generar PaymentValidated
24. Apagar PostgreSQL
25. Reanudar Scoring Consumer
26. Mostrar Retry → DLT
27. Levantar PostgreSQL
28. Reprocesar DLT
29. Mostrar ScoringCalculated
30. Mostrar Gmail + PDF
31. Mostrar SASL_SSL / ACL
32. mvn clean verify
```

---

# 29. Matriz concepto → prueba

| Concepto Kafka | Prueba |
|---|---|
| KRaft | 2 |
| Topic | 3 |
| Partition | 3, 4, 8 |
| Key | 4, 5 |
| Offset | 4, 7, 13 |
| Producer | 4, 12 |
| Consumer | 6 |
| Consumer Group | 7, 8 |
| Competing Consumers | 8 |
| Rebalance | 9 |
| Consumer Lag | 7 |
| Retention/backlog | 6 |
| Event tracing | 5 |
| Transactional Outbox | 11 |
| `acks=all` | 12 |
| Idempotent Producer | 12 |
| At-least-once | 13 |
| Replay | 13 |
| Idempotent Consumer | 14 |
| Retry | 15, 16 |
| DLT | 16 |
| DLT reprocess | 17 |
| Error de negocio vs técnico | 18 |
| SASL_SSL | 20 |
| ACL | 21 |
| TLS | 20, 22 |
| Flyway | 24 |
| Clean Architecture | 25 |

---

# 30. Nota sobre Exactly-Once

El proyecto **no configura Kafka Transactions / Exactly-Once** deliberadamente.

Se utiliza:

```text
Outbox
+ at-least-once
+ processed_event
+ Idempotent Consumer
+ Retry/DLT
```

porque el flujo incluye PostgreSQL y Gmail SMTP. Kafka Exactly-Once es especialmente apropiado para `Kafka → procesamiento → Kafka`, pero no puede hacer rollback de un correo que Gmail ya aceptó.

---

# 31. Reset completo del laboratorio

Cuando quieras repetir todas las pruebas desde cero:

```bash
docker compose down -v
./scripts/generate-dev-certs.sh
docker compose up -d --build
```

Esto elimina los volúmenes PostgreSQL/Kafka, vuelve a ejecutar Flyway y recrea los pagos simulados como no utilizados.


## Datos precargados en Angular para la demostración

El formulario Angular se inicia con un escenario válido para las pruebas de éxito (DNI `12345678`, `Cliente Demo Kafka`, datos financieros válidos, producto `PRESTAMO_PERSONAL`, banco `BCP`, operación `PAGO-DEMO-BCP-001`, monto `30 PEN` y fecha actual). **El correo destinatario queda vacío a propósito y se ingresa dinámicamente en Angular para cada solicitud.** Para una demostración end-to-end, escribe un correo real donde quieras recibir el PDF. `GMAIL_USERNAME` sigue siendo únicamente la cuenta emisora configurada de forma estática en el backend.
