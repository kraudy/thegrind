
-- =============================================
-- Compone la configuracion de las molduras varillas porque la priemera version no generaba todas las combinaciones
-- =============================================

DELETE from producto_config  where modelo in ('Varilla', 'VarillaGruesa');

INSERT INTO producto_medida (medida, descripcion)
SELECT medida, descripcion 
FROM (VALUES
  ('10x12','Medida 10x12')
  
) AS valores(medida, descripcion);

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT
  'Molduras',
  st.sub_tipo,
  m.medida,
  mod.modelo,
  col.color
FROM (VALUES ('Normal'), ('Reparacion'), ('Vacio')) AS st(sub_tipo)
CROSS JOIN (VALUES ('24x30'), ('20x24'), ('16x24'), ('16x20'), ('13x19'), ('12x18'), ('12x16'), ('11x17'), ('11x14'), ('10x15'), ('10x12'), ('8.5x11'), ('8x10'), ('6x8'), ('4x6')) AS m(medida)
CROSS JOIN (VALUES ('Varilla'), ('VarillaGruesa')) AS mod(modelo)
CROSS JOIN (VALUES ('Nogal'), ('Cristobal'), ('Blanco'), ('Negro'), ('Natural')) AS col(color)
;