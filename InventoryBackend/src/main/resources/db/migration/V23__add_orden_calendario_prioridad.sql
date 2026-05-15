-- Add prioridad column to orden_calendario
ALTER TABLE orden_calendario
  ADD COLUMN prioridad VARCHAR(20) NOT NULL DEFAULT 'Normal';

ALTER TABLE orden_calendario
  ADD CONSTRAINT chk_orden_calendario_prioridad
  CHECK (prioridad IN ('Normal', 'Alta', 'Urgente'));

CREATE INDEX idx_orden_calendario_prioridad ON orden_calendario (prioridad);
