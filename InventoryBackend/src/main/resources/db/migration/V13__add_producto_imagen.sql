
-- =============================================
-- Agrega imagen a producto
-- =============================================
ALTER TABLE producto 
ADD COLUMN IF NOT EXISTS imagen VARCHAR(255) DEFAULT NULL;

COMMENT ON COLUMN producto.imagen IS 'Ruta relativa de la imagen. Opcional.';
