-- Garantiza idempotencia a nivel de base de datos:
-- una orden solo puede tener una factura asociada.
ALTER TABLE factura
  ADD CONSTRAINT uq_factura_id_orden UNIQUE (id_orden);
