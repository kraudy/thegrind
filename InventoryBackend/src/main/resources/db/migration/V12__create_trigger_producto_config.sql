
-- =============================================
-- Agrega campos de auditoria a producto, producto_precio y producto_costo
-- =============================================

ALTER TABLE producto 
  ADD COLUMN usuario_creacion     VARCHAR(50) NOT NULL DEFAULT 'system',
  ADD COLUMN usuario_modificacion VARCHAR(50) NOT NULL DEFAULT 'system'
;

ALTER TABLE producto_precio 
  ADD COLUMN usuario_creacion     VARCHAR(50) NOT NULL DEFAULT 'system',
  ADD COLUMN usuario_modificacion VARCHAR(50) NOT NULL DEFAULT 'system'
;

ALTER TABLE producto_costo 
  ADD COLUMN usuario_creacion     VARCHAR(50) NOT NULL DEFAULT 'system',
  ADD COLUMN usuario_modificacion VARCHAR(50) NOT NULL DEFAULT 'system'
;

-- =============================================
-- VALIDACIÓN DE COMBINACIONES VÁLIDAS (producto_config)
-- =============================================

CREATE OR REPLACE FUNCTION validar_producto_config()
RETURNS TRIGGER AS $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 
    FROM producto_config
    WHERE tipo          = NEW.tipo_producto
      AND sub_tipo      = NEW.sub_tipo_producto
      AND medida        = NEW.medida_producto
      AND modelo        = NEW.modelo_producto
      AND color         = NEW.color_producto
  ) THEN
    RAISE EXCEPTION 'Combinación inválida de atributos en producto: % | % | % | % | %', 
      NEW.tipo_producto, 
      NEW.sub_tipo_producto, 
      NEW.medida_producto, 
      NEW.modelo_producto, 
      NEW.color_producto
      USING HINT = 'Verifica que la combinación de atributos exista';
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger que se ejecuta ANTES de cualquier INSERT o UPDATE
CREATE TRIGGER trg_validar_producto_config
  BEFORE INSERT OR UPDATE ON producto
  FOR EACH ROW
  EXECUTE FUNCTION validar_producto_config();

-- Opcional: Comentario para que quede claro en el esquema
COMMENT ON TRIGGER trg_validar_producto_config ON producto 
IS 'Valida que la combinación de atributos del producto exista en producto_config';
