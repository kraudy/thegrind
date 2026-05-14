
-- =============================================
-- Agrega columna canal (General | Whatsapp) a orden
-- =============================================

ALTER TABLE orden
  ADD COLUMN canal VARCHAR(20) NOT NULL DEFAULT 'General';

ALTER TABLE orden
  ADD CONSTRAINT chk_orden_canal
  CHECK (canal IN ('General', 'Whatsapp'));
