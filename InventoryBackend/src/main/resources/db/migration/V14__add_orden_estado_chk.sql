
-- =============================================
-- Agrega check constraint para estado de orden
-- =============================================

ALTER TABLE orden 
DROP CONSTRAINT fk_orden_estado_seguimiento;

-- Add the CHECK constraint with your allowed states
ALTER TABLE orden 
ADD CONSTRAINT chk_orden_estado 
CHECK (estado IN ('Recibida', 'Repartida', 'Listo', 'Entregado', 'Facturado'));
