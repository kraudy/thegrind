
-- =============================================
-- Agregar 6x8 a moldura config
-- =============================================

-- Eliminar configuracion previa
DELETE from producto_config where tipo = 'Molduras' and modelo = '1040';

-- Reconfigurar molduras 1040 con 6x8

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Molduras',
  st.sub_tipo,
  m.medida,
  mod.modelo,
  'Dorada'
FROM (VALUES ('Normal'), ('Reparacion'), ('Vacio')) AS st(sub_tipo)
CROSS JOIN (VALUES ('12x16'), ('11x17'), ('11x14'), ('10x15'), ('8.5x11'), ('8x10'), ('6x8')) AS m(medida)
CROSS JOIN (VALUES ('1040')) AS mod(modelo)
;