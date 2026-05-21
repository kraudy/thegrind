
-- =============================================
-- Agregar ovalado 
-- =============================================

INSERT INTO producto_modelo (modelo, descripcion)
SELECT modelo, descripcion 
FROM (VALUES
  ('Ovalado','Retablo/Tabla ovalado')
) AS valores(modelo, descripcion);

DELETE from producto_config where tipo = 'Retablos';

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Retablos',
  st.sub_tipo,
  m.medida,
  mod.modelo,
  c.color
FROM (VALUES ('Normal'), ('Reparacion')) AS st(sub_tipo)
CROSS JOIN (VALUES ('24x30'), ('20x24'), ('16x24'), ('16x20'), ('13x19'), ('12x18'), ('12x16'), ('11x17'), ('11x14'), ('10x15'), ('8.5x11'), ('8x10'), ('6x8'), ('4x6')) AS m(medida)
CROSS JOIN (VALUES ('Sencillo'), ('Especial'), ('Mariposa'), ('Ovalado')) AS mod(modelo)
CROSS JOIN (VALUES ('Rojo'), ('Negro'), ('Natural')) AS c(color);

