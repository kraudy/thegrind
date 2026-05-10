
-- =============================================
-- Agrega check metodo de pago a orden_pago
-- =============================================

ALTER TABLE orden_pago
ADD CONSTRAINT chk_orden_pago_metodo_pago
CHECK (metodo_pago IN ('Efectivo', 'Transferencia', 'Tarjeta'));