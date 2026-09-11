-- Nullable inicialmente para no romper registros históricos.
-- Toda nueva solicitud autenticada debe persistir JWT.sub.
ALTER TABLE solicitud_scoring_workflow
    ADD COLUMN IF NOT EXISTS usuario_app_id BIGINT;

ALTER TABLE solicitud_scoring_workflow
    ADD CONSTRAINT fk_scoring_workflow_usuario_app
    FOREIGN KEY (usuario_app_id) REFERENCES usuarios_app(usuario_app_id);

CREATE INDEX IF NOT EXISTS idx_scoring_workflow_usuario_app
    ON solicitud_scoring_workflow(usuario_app_id);

-- Después del backfill de históricos:
-- ALTER TABLE solicitud_scoring_workflow ALTER COLUMN usuario_app_id SET NOT NULL;
