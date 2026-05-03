
-- =============================================
-- Fix Calado estados and prodcuto config to include Pegado and Normal, Reparacion subtypes
-- =============================================

DELETE from producto_tipo_estado where tipo = 'Calado';

INSERT INTO producto_tipo_estado (tipo, sub_tipo, estado, secuencia, usuario_creacion, usuario_modificacion)
WITH logica_estados (tipo, sub_tipo, estado, secuencia) AS (
  VALUES

  ('Calado'    , 'Normal'    , 'Repartida'  , 1),
  ('Calado'    , 'Normal'    , 'Normal'     , 2),
  ('Calado'    , 'Normal'    , 'Impresion'  , 3),
  ('Calado'    , 'Normal'    , 'Calado'     , 4),
  ('Calado'    , 'Normal'    , 'Pegado'     , 5),
  ('Calado'    , 'Normal'    , 'Listo'      , 6),
  ('Calado'    , 'Normal'    , 'Entregado'  , 7),

  ('Calado'    , 'Reparacion', 'Repartida'  , 1),
  ('Calado'    , 'Reparacion', 'Reparacion' , 2),
  ('Calado'    , 'Reparacion', 'Impresion'  , 3),
  ('Calado'    , 'Reparacion', 'Calado'     , 4),
  ('Calado'    , 'Reparacion', 'Pegado'     , 5),
  ('Calado'    , 'Reparacion', 'Listo'      , 6),
  ('Calado'    , 'Reparacion', 'Entregado'  , 7)
)
SELECT logica_estados.tipo, logica_estados.sub_tipo, logica_estados.estado, logica_estados.secuencia, 'system', 'system' FROM logica_estados;


DELETE from producto_config  where tipo = 'Calado';

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Calado',
  st.sub_tipo,
  m.medida,
  mod.modelo,
  'Ninguno'
FROM (VALUES ('Normal'), ('Reparacion')) AS st(sub_tipo)
CROSS JOIN (VALUES ('4x3'), ('4x6'), ('6x9')) AS m(medida)
CROSS JOIN (VALUES ('ConBase'),('SinBase')) AS mod(modelo);

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Calado',
  st.sub_tipo,
  m.medida,
  mod.modelo,
  'Ninguno'
FROM (VALUES ('Normal'), ('Reparacion')) AS st(sub_tipo)
CROSS JOIN (VALUES ('Pequeña'), ('Mediana'), ('Grande')) AS m(medida)
CROSS JOIN (VALUES ('Repisa')) AS mod(modelo);