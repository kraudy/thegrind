
-- =============================================
-- Agregar 20x30 a ampliacion config
-- =============================================

INSERT INTO producto_medida (medida, descripcion)
SELECT medida, descripcion 
FROM (VALUES
  ('20x30','Medida 20x30')
) AS valores(medida, descripcion);

-- Elimina configuracion previa
DELETe from producto_config where tipo = 'Ampliaciones';

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Ampliaciones',
  st.sub_tipo,
  m.medida,
  mod.modelo,
  'Ninguno'
FROM (VALUES ('Normal'), ('Reparacion')) AS st(sub_tipo)
CROSS JOIN (VALUES ('24x30'), ('20x30'), ('20x24'), ('16x24'), ('16x20'), ('13x19'), ('12x18'), ('12x16'), ('11x17'), ('11x14'), ('10x15'), ('8.5x11'), ('8x10'), ('6x8'), ('4x6')) AS m(medida)
CROSS JOIN (VALUES ('Ninguno'), ('Fotografica')) AS mod(modelo)
;
