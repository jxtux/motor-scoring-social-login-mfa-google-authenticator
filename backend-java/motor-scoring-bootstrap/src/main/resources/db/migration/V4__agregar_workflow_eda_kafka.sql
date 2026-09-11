-- Estructuras de proceso para EDA. No reemplazan al dominio de scoring.

CREATE TABLE solicitud_scoring_workflow (
    solicitud_scoring_id VARCHAR(36) PRIMARY KEY,
    id_solicitud BIGINT NOT NULL UNIQUE,
    id_solicitante BIGINT NOT NULL,
    correlation_id VARCHAR(36) NOT NULL,
    correo_electronico VARCHAR(200) NOT NULL,
    banco VARCHAR(30) NOT NULL,
    numero_operacion VARCHAR(80) NOT NULL,
    monto_pagado DECIMAL(18,2) NOT NULL,
    moneda_pago VARCHAR(10) NOT NULL,
    fecha_pago DATE NOT NULL,
    estado_proceso VARCHAR(40) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL,
    CONSTRAINT fk_workflow_solicitud FOREIGN KEY(id_solicitud) REFERENCES solicitudes_credito(id_solicitud),
    CONSTRAINT fk_workflow_solicitante FOREIGN KEY(id_solicitante) REFERENCES solicitantes(id_solicitante)
);

CREATE TABLE pago_simulado (
    pago_simulado_id UUID PRIMARY KEY,
    banco VARCHAR(30) NOT NULL,
    numero_operacion VARCHAR(80) NOT NULL,
    monto DECIMAL(18,2) NOT NULL,
    moneda VARCHAR(10) NOT NULL,
    fecha_pago DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    utilizado BOOLEAN NOT NULL DEFAULT FALSE,
    solicitud_scoring_id VARCHAR(36),
    CONSTRAINT uk_pago_simulado_operacion UNIQUE(banco, numero_operacion)
);

CREATE TABLE outbox_event (
    outbox_event_id VARCHAR(36) PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL UNIQUE,
    event_type VARCHAR(80) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    topic VARCHAR(120) NOT NULL,
    event_key VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_outbox_status_created ON outbox_event(status, created_at);

CREATE TABLE processed_event (
    processed_event_id UUID PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    consumer_name VARCHAR(100) NOT NULL,
    topic VARCHAR(120) NOT NULL,
    kafka_partition INT NOT NULL,
    kafka_offset BIGINT NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_processed_event_consumer UNIQUE(event_id, consumer_name)
);
