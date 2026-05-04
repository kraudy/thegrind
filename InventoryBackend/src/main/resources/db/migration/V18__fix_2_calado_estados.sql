
-- =============================================
-- Remover estado calado de Caldado porque es parte del proceso de produccion que se realizara aparte
-- =============================================

DELETE from producto_tipo_estado where tipo = 'Calado';

INSERT INTO producto_tipo_estado (tipo, sub_tipo, estado, secuencia, usuario_creacion, usuario_modificacion)
WITH logica_estados (tipo, sub_tipo, estado, secuencia) AS (
  VALUES

  ('Calado'    , 'Normal'    , 'Repartida'  , 1),
  ('Calado'    , 'Normal'    , 'Normal'     , 2),
  ('Calado'    , 'Normal'    , 'Impresion'  , 3),
  ('Calado'    , 'Normal'    , 'Pegado'     , 4),
  ('Calado'    , 'Normal'    , 'Listo'      , 5),
  ('Calado'    , 'Normal'    , 'Entregado'  , 6),

  ('Calado'    , 'Reparacion', 'Repartida'  , 1),
  ('Calado'    , 'Reparacion', 'Reparacion' , 2),
  ('Calado'    , 'Reparacion', 'Impresion'  , 3),
  ('Calado'    , 'Reparacion', 'Pegado'     , 4),
  ('Calado'    , 'Reparacion', 'Listo'      , 5),
  ('Calado'    , 'Reparacion', 'Entregado'  , 6)
  
)
SELECT logica_estados.tipo, logica_estados.sub_tipo, logica_estados.estado, logica_estados.secuencia, 'system', 'system' FROM logica_estados;