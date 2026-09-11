# Motor de Scoring Crediticio — Clean Architecture + Apache Kafka

Evolución del proyecto original de scoring crediticio hacia una arquitectura **Event-Driven Architecture (EDA)** con Apache Kafka, manteniendo el núcleo y las reglas de negocio existentes.

La aplicación recibe desde Angular una solicitud de score crediticio y datos de un pago bancario simulado. El backend registra la solicitud, publica eventos mediante **Transactional Outbox**, valida el pago de forma asíncrona, ejecuta el mismo motor de scoring del proyecto original y finalmente genera un PDF **en memoria** que se envía por Gmail SMTP.

> **Regla de evolución aplicada:** el código del dominio que no necesitaba cambiar se conserva. Kafka, PostgreSQL, SMTP, PDF y REST permanecen en las capas externas.

---

## 1. Tecnologías

- Java 21
- Spring Boot 3.5.x
- Maven multimódulo
- Angular
- PostgreSQL como **única base de datos**
- Apache Kafka en modo KRaft, 1 nodo para laboratorio
- 3 partitions por topic
- Docker Compose
- Flyway
- Spring Kafka
- PDFBox
- Gmail SMTP
- Kafka UI
- JUnit 5, ArchUnit y Testcontainers

---

## 2. Arquitectura general

```text
Angular
   │ HTTPS
   ▼
motor-scoring-presentation
   │
   ▼
motor-scoring-application
   │
   ▼
motor-scoring-domain
   │
   ├─────────────── PostgreSQL
   │                    │
   │               outbox_event
   │                    │
   │              OutboxPublisher
   │                    │ SASL_SSL
   │                    ▼
   │               Apache Kafka
   │           3 partitions por topic
   │                    │
   │     ┌──────────────┼────────────────┐
   │     ▼              ▼                ▼
   │ PaymentConsumer ScoringConsumer DeliveryConsumer
   │     │              │                │
   │     │              └──► Domain      ├──► PDF byte[]
   │     │                   existente   └──► Gmail SMTP
   │     │
   └─────┴──────────────────────────────────────────────
```

### Clean Architecture

```text
┌────────────────────────────────────────────────────────────┐
│ EXTERIOR                                                   │
│ Angular · REST · Kafka · PostgreSQL · SMTP · PDF · Docker │
│                                                            │
│   ┌────────────────────────────────────────────────────┐   │
│   │ Presentation / Infrastructure                     │   │
│   │                                                    │   │
│   │     ┌────────────────────────────────────────┐     │   │
│   │     │ Application                            │     │   │
│   │     │                                        │     │   │
│   │     │      ┌──────────────────────────┐      │     │   │
│   │     │      │ DOMAIN — NÚCLEO          │      │     │   │
│   │     │      │ Entidades · VO · Reglas │      │     │   │
│   │     │      │ CalculadorScoring        │      │     │   │
│   │     │      └──────────────────────────┘      │     │   │
│   │     └────────────────────────────────────────┘     │   │
│   └────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────┘
```

Las dependencias apuntan hacia el núcleo. `domain` no conoce Kafka, Spring, JPA, PostgreSQL ni SMTP.

---

## 3. Composición del proyecto

```text
motor-scoring/
│
├── backend-java/
│   ├── motor-scoring-domain/
│   │   └── entidades, Value Objects, reglas y cálculo del score
│   │
│   ├── motor-scoring-application/
│   │   ├── casos de uso existentes
│   │   ├── casos de uso EDA
│   │   ├── commands / DTO
│   │   └── ports de salida
│   │
│   ├── motor-scoring-presentation/
│   │   └── REST, validación HTTP y OpenAPI
│   │
│   ├── motor-scoring-infrastructure/
│   │   ├── persistence/       PostgreSQL / JPA
│   │   ├── messaging/kafka/   producer, consumers, DLT, Outbox
│   │   ├── report/            PDFBox
│   │   ├── notification/      Gmail SMTP
│   │   └── transaction/       wrappers transaccionales locales
│   │
│   └── motor-scoring-bootstrap/
│       ├── Composition Root
│       ├── configuración
│       └── Flyway migrations
│
├── frontend-angular/
├── scripts/
├── docs/
├── docker-compose.yml
├── README.md
└── README_PRUEBAS.md
```

### Qué permanece del núcleo

Se mantienen las reglas y clases existentes como:

- `SolicitudCredito`
- `Solicitante`
- `EvaluacionCrediticia`
- `ProductoCrediticio`
- `ModeloScoring`
- `VersionModelo`
- `FactorScoring`
- `ReglaEvaluacion`
- `ResultadoFactor`
- `CalculadorScoring`
- `CalculadorCapacidadPago`
- `CalculadorRelacionDeudaIngreso`
- `CalculadorRelacionCuotaIngreso`
- `EvaluadorReglasExcluyentes`
- Value Objects financieros y de identificación

---

## 4. Procesos Spring Boot ejecutables

El proyecto utiliza **el mismo JAR y la misma imagen Docker** para distintos procesos. La separación es de ejecución, no una duplicación del código fuente.

| Proceso | Spring Profile | Responsabilidad |
|---|---|---|
| `motor-scoring-api` | `postgres,api` | REST para Angular + migraciones Flyway |
| `outbox-publisher` | `postgres,outbox-publisher` | publicar `outbox_event=PENDING` en Kafka |
| `payment-consumer` | `postgres,payment-consumer` | validar pago simulado |
| `scoring-consumer` | `postgres,scoring-consumer` | ejecutar el dominio de scoring |
| `credit-score-delivery-consumer` | `postgres,delivery-consumer` | generar PDF en memoria + Gmail SMTP |
| `dlt-reprocessor` | `postgres,dlt-reprocessor` | herramienta administrativa de reproceso DLT |

Por esta razón es posible ejecutar:

```bash
docker compose stop outbox-publisher
docker compose start outbox-publisher
```

sin detener la API.

---

## 5. Flujo EDA

```text
POST /api/v1/scoring-requests
       ↓
PostgreSQL
solicitud + workflow + Outbox ScoringRequested
       ↓
OutboxPublisher
       ↓
scoring.requested.v1
       ↓
payment-validation-group
       ↓
PaymentValidated / PaymentRejected
       ↓
payment.validated.v1
       ↓
scoring-calculation-group
       ↓
Application
       ↓
DOMAIN EXISTENTE
CalculadorScoring
       ↓
evaluación + Outbox ScoringCalculated
       ↓
scoring.calculated.v1
       ↓
credit-score-delivery-group
       ↓
PDF byte[] EN MEMORIA
       ↓
Gmail SMTP
       ↓
Usuario recibe PDF
       ↓
Outbox CreditScoreDelivered
```

---

## 6. Identificadores y trazabilidad

### `solicitudScoringId`

- generado por el backend con UUID;
- identifica una solicitud de negocio;
- es único por solicitud, incluso para el mismo cliente;
- se utiliza como **Kafka key**.

### Kafka `key`

```text
key = solicitudScoringId
```

Kafka utiliza la key para determinar la partition. Records con la misma key dentro del mismo topic se asignan consistentemente a la misma partition mientras se mantenga el esquema de particionado.

### `eventId`

Identifica un evento individual. Cada evento nuevo tiene otro `eventId`.

### `correlationId`

Permanece durante todo el proceso y permite reconstruir el flujo distribuido.

```text
ScoringRequested      eventId=A ─┐
PaymentValidated      eventId=B  ├─ correlationId=X
ScoringCalculated     eventId=C  │
CreditScoreDelivered  eventId=D ─┘
```

### `causationId`

Contiene el `eventId` del evento que originó el evento actual.

---

## 7. Topics, partitions y Consumer Groups

Topics principales:

```text
scoring.requested.v1
payment.validated.v1
payment.rejected.v1
scoring.calculated.v1
credit-score.delivered.v1
```

DLT:

```text
scoring.requested.v1.DLT
payment.validated.v1.DLT
scoring.calculated.v1.DLT
```

Todos se crean con:

```text
partitions = 3
replication.factor = 1
```

Consumer Groups:

```text
payment-validation-group
scoring-calculation-group
credit-score-delivery-group
```

En ejecución normal se inicia una instancia por servicio. Para demostrar paralelismo se pueden escalar tres instancias con `concurrency=1`:

```bash
docker compose up -d --scale payment-consumer=3 payment-consumer
```

Con tres partitions pueden existir como máximo tres consumers activos procesando partitions en paralelo dentro de ese group.

---

## 8. PostgreSQL

PostgreSQL es la **única base de datos** del proyecto.

Tablas principales del dominio ya existente:

```text
solicitantes
solicitudes_credito
productos_crediticios
modelos_scoring
versiones_modelo
factores_scoring
reglas_evaluacion
evaluaciones_crediticias
resultados_factor
```

Tablas añadidas para el workflow EDA:

```text
solicitud_scoring_workflow
pago_simulado
outbox_event
processed_event
```

No existen tablas para:

```text
voucher
imagen del voucher
PDF
archivo de reporte
historial del correo enviado
```

---

## 9. Transactional Outbox

Cuando una operación PostgreSQL debe originar un evento:

```text
BEGIN

cambio de negocio
+
INSERT outbox_event status=PENDING

COMMIT
```

Posteriormente:

```text
PENDING
  ↓
OutboxPublisher
  ↓
Kafka
  ↓ ACK
PUBLISHED
```

Si Kafka está caído, el cambio de negocio ya está protegido por PostgreSQL y el evento continúa `PENDING` para reintento posterior.

---

## 10. At-least-once + Idempotent Consumer

Los consumers procesan bajo el modelo **at-least-once**. Por ello deben tolerar que un mismo record sea entregado nuevamente.

`processed_event` registra:

```text
event_id
consumer_name
topic
kafka_partition
kafka_offset
processed_at
```

con:

```text
UNIQUE(event_id, consumer_name)
```

Si el mismo `eventId` vuelve a llegar:

```text
status=ALREADY_PROCESSED
action=SKIP
```

El procedimiento completo para demostrarlo mediante reset de offsets está en [`README_PRUEBAS.md`](README_PRUEBAS.md).

---

## 11. Retry y DLT

Errores técnicos reciben reintentos limitados con backoff. Al agotarse:

```text
Topic original
   ↓
Retry
   ↓
Retry
   ↓
DLT
```

Errores de negocio, por ejemplo pago no encontrado o pago ya utilizado, no se consideran fallos técnicos y producen `PaymentRejected`.

El `DltReprocessor` permite republicar un record desde el DLT hacia su topic original después de corregir la causa. El record histórico del DLT no se borra inmediatamente: Kafka lo conserva según retention.

---

## 12. Delivery semantics

Este proyecto utiliza:

```text
Producer:
acks=all
+ enable.idempotence=true

Consumer:
at-least-once
+ processed_event
+ Idempotent Consumer

PostgreSQL → Kafka:
Transactional Outbox
```

No se configura Kafka Exactly-Once/transactions porque el flujo incluye sistemas externos como PostgreSQL y Gmail SMTP. Kafka Exactly-Once es especialmente fuerte en pipelines `Kafka → procesamiento → Kafka`, pero no hace atómica una operación que también involucra un correo ya enviado por SMTP.

---

## 13. PDF de score crediticio

El PDF **no se guarda**.

```text
ScoringCalculated
      ↓
PdfGenerator
      ↓
byte[] en memoria
      ↓
EmailSender
      ↓
Gmail SMTP
      ↓
byte[] descartado
```

El informe contiene:

1. **Datos del solicitante**
   - nombre;
   - tipo de documento;
   - documento enmascarado;
   - fecha de evaluación.
2. **Resultado**
   - score sobre 1000;
   - resultado del dominio (`PREAPROBADA`, `REVISION_MANUAL`, `RECHAZADA`);
   - estado de evaluación.
3. **Producto / solicitud**
   - producto;
   - monto;
   - plazo;
   - moneda.
4. **Resumen financiero**
   - ingresos;
   - gastos;
   - obligaciones;
   - capacidad de pago;
   - relación deuda/ingreso;
   - relación cuota/ingreso.
5. **Factores evaluados**
   - valor;
   - peso;
   - puntaje;
   - regla aplicada;
   - observación;
   - indicador de regla excluyente.
6. **Versión del modelo**.
7. Aviso informativo.

No contiene `eventId`, `correlationId`, offset, partition ni detalles internos de Kafka.

---

## 14. Validación bancaria simulada

Angular no sube un voucher. Ingresa:

```text
banco
número de operación
monto pagado
moneda
fecha de pago
```

Flyway precarga pagos de laboratorio, por ejemplo:

```text
BCP        PAGO-DEMO-BCP-001 ... 005
BBVA       PAGO-DEMO-BBVA-001 ... 005
INTERBANK  PAGO-DEMO-INTERBANK-001 ... 005
Monto: PEN 30.00
Fecha: CURRENT_DATE al crear la base
```

El pago se reclama mediante un `UPDATE ... WHERE utilizado=false` para evitar que dos consumers utilicen concurrentemente la misma operación.

---

## 15. Seguridad

### API

```text
Angular ↔ Backend = HTTPS/TLS
```

### Kafka

```text
SASL_SSL
= SASL/SCRAM-SHA-512 + TLS
```

ACL con mínimo privilegio por principal:

```text
outbox-publisher
payment-validation
scoring-calculation
credit-score-delivery
dlt-reprocessor
kafka-ui
```

### PostgreSQL

La aplicación utiliza JDBC con TLS y `sslmode=verify-full`.

### Gmail

SMTP:

```text
smtp.gmail.com:587
STARTTLS
```

Las credenciales se reciben por variables de entorno. **No se deben almacenar contraseñas en Git.**

---

# 16. Cómo levantar el proyecto

## Requisitos

- Docker Engine / Docker Desktop con Docker Compose v2
- OpenSSL
- JDK 21 (`keytool`) para generar certificados locales

No necesitas instalar PostgreSQL, Kafka, Node ni Maven en el host para ejecutar el laboratorio completo.

## Paso 1 — Variables

```bash
cp .env.example .env
```

Edita `.env` y completa, como mínimo para el envío real:

```text
GMAIL_USERNAME=tu-cuenta@gmail.com
GMAIL_APP_PASSWORD=tu-app-password
```

Para Gmail se recomienda usar una **App Password** de Google según las políticas de la cuenta; no uses la contraseña normal.

## Paso 2 — Certificados TLS de desarrollo

```bash
./scripts/generate-dev-certs.sh
```

Esto crea certificados únicamente para laboratorio en:

```text
backend-java/docker/certs/
```

Para evitar la advertencia del navegador al abrir la API HTTPS, importa:

```text
backend-java/docker/certs/ca.crt
```

como CA confiable del entorno local.

## Paso 3 — Construir y levantar

```bash
docker compose up -d --build
```

## Paso 4 — Ver estado

```bash
docker compose ps
```

## Paso 5 — Logs

```bash
docker compose logs -f motor-scoring-api
docker compose logs -f outbox-publisher
docker compose logs -f payment-consumer
docker compose logs -f scoring-consumer
docker compose logs -f credit-score-delivery-consumer
```

## URLs

| Componente | URL |
|---|---|
| Angular | http://localhost:4200 |
| API HTTPS | https://localhost:8443 |
| Swagger | https://localhost:8443/swagger-ui.html |
| Kafka UI | http://localhost:8085 |
| Kafka externo | localhost:9092 / SASL_SSL |
| PostgreSQL | localhost:5432 / TLS |

---

## 17. Detener o levantar componentes individualmente

```bash
docker compose stop outbox-publisher
docker compose start outbox-publisher

docker compose stop payment-consumer
docker compose start payment-consumer

docker compose stop scoring-consumer
docker compose start scoring-consumer
```

### Escalar un Consumer Group

```bash
docker compose up -d --scale payment-consumer=3 payment-consumer
```

### Apagar todo conservando datos

```bash
docker compose down
```

### Reiniciar completamente el laboratorio

```bash
docker compose down -v
./scripts/generate-dev-certs.sh
docker compose up -d --build
```

---

## 18. Solicitud por consola

```bash
./scripts/send-demo-request.sh tu-correo@gmail.com 12345678 PAGO-DEMO-BCP-001
```

O usa Angular en `http://localhost:4200`.

---

## 19. Utilidades de demostración

```bash
./scripts/show-outbox.sh
./scripts/show-processed-events.sh
./scripts/consume-topic.sh scoring.requested.v1
./scripts/describe-consumer-group.sh scoring-calculation-group
```

Reprocesar DLT:

```bash
./scripts/reprocess-dlt.sh payment.validated.v1.DLT <eventId>
```

Reset de offset para demostrar replay/idempotencia:

```bash
./scripts/reset-consumer-offset.sh \
  scoring-calculation-group payment.validated.v1 1 12
```

Ver [`README_PRUEBAS.md`](README_PRUEBAS.md) para el procedimiento completo.

---

## 20. Compilar y ejecutar tests fuera de Docker

Con Maven + JDK 21:

```bash
cd backend-java
mvn clean verify
```

Los tests de Testcontainers requieren Docker disponible.

La imagen Docker de runtime compila con `-DskipTests`; las pruebas se ejecutan deliberadamente como una etapa separada para no intentar levantar Testcontainers dentro del build de la imagen.

---

## 21. Flyway

Flyway versiona el esquema PostgreSQL junto con el código:

```text
V1__crear_modelo_datos.sql
V2__insertar_modelo_scoring_inicial.sql
V3__agregar_factor_relacion_cuota_ingreso.sql
V4__agregar_workflow_eda_kafka.sql
V5__insertar_pagos_simulados.sql
```

Spring Boot ejecuta las migraciones desde `motor-scoring-api`. Los workers las tienen desactivadas y esperan a que la API esté healthy antes de arrancar.

---

## 22. Documentación de pruebas Kafka

El guion de demostración de Topics, Partitions, Keys, Offsets, Consumer Groups, Rebalance, Lag, Outbox, at-least-once, Idempotent Consumer, Retry, DLT, reproceso, seguridad y consola se encuentra en:

**[`README_PRUEBAS.md`](README_PRUEBAS.md)**

---

## 23. Documentos históricos

El directorio `docs/` proviene del proyecto original y contiene material de etapas anteriores (Onion/Hexagonal, H2 y MongoDB). Se conserva como evidencia histórica, pero **la arquitectura ejecutable actual descrita en este README usa únicamente PostgreSQL + Kafka**.


## Configuración Gmail y datos demo

El envío usa Gmail SMTP. Hay que diferenciar claramente **emisor** y **destinatario**:

- `GMAIL_USERNAME` es la cuenta **emisora** y es una configuración estática del backend.
- `GMAIL_APP_PASSWORD` es la contraseña de aplicación de Google usada por Spring Boot para autenticarse por SMTP. **No es la contraseña normal de Gmail**.
- El **correo destinatario no se configura en `.env`**: es dinámico y lo escribe el usuario en Angular para cada solicitud.

En `.env` solamente configura el emisor:

```properties
GMAIL_USERNAME=tu-cuenta-emisora@gmail.com
GMAIL_APP_PASSWORD=abcdefghijklmnop
```

El formulario Angular queda precargado con un escenario válido de laboratorio, excepto el correo destinatario, que queda vacío y es obligatorio:

```text
Tipo documento: DNI
Número documento: 12345678
Nombre: Cliente Demo Kafka
Correo destinatario: [ingresado por el usuario]
Ingresos: 5500
Gastos: 1800
Obligaciones: 700
Antigüedad: 36 meses
Obligaciones activas: 2
Historial de pagos: 85
Alertas de mora: 0
Producto: PRESTAMO_PERSONAL
Monto solicitado: 15000 PEN
Plazo: 24 meses
Finalidad: Consumo
Banco: BCP
Operación: PAGO-DEMO-BCP-001
Monto pagado: 30 PEN
Fecha de pago: fecha actual
```

Para una prueba exitosa, abre Angular, ingresa un correo real en **Correo donde recibirá el informe PDF** y envía la solicitud. Ese valor viaja dinámicamente en el request y se utiliza como `TO` del correo final. La cuenta `GMAIL_USERNAME` se utiliza como `FROM`.

No guardar credenciales reales en Git.

---

# 22. Seguridad IAM integrada

Esta distribución incluye también autenticación local, Google OAuth2/OIDC, TikTok OAuth2, MFA TOTP, JWT RSA, Refresh Token HttpOnly y RBAC, manteniendo el dominio y el flujo Kafka/Outbox originales.

Consulta la guía de ejecución y configuración:

```text
README_SEGURIDAD_IAM.md
```

Antes de levantar una instalación limpia, ejecuta nuevamente `scripts/generate-dev-certs.sh`: además del TLS original, genera las claves RSA del JWT y los secretos locales de TOTP/verificación requeridos por la API.

---

## FIXED8 - continuidad del registro y MFA

Esta entrega corrige la transición de verificación de correo hacia Google Authenticator, permite reanudar usuarios registrados a medias y unifica el post-login de Google/TikTok con MFA obligatorio. Consulta `FIXED8_NOTES.md` para el detalle y las pruebas realizadas.
