
-- Add tipo pago to orden_pago to allow adelantos and regular payments

ALTER TABLE orden_pago
  ADD COLUMN tipo_pago VARCHAR(20) NOT NULL,
  ADD CONSTRAINT chk_tipo_pago CHECK (tipo_pago IN ('Adelanto', 'Saldo'))
;
