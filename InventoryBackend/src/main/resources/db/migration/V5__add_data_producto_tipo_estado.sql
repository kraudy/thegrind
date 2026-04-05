
-- Estados de seguimiento
INSERT INTO estado_seguimiento (estado, descripcion, usuario_creacion, usuario_modificacion)
WITH estados_seg (estado, descripcion) AS (
  VALUES
  ('Bodega'       , 'Estado de trabajo para productos que se encuentran en bodega'),
  ('Armado'       , 'Estado de trabajo para productos que requieren armado'),
  ('Sublimacion'  , 'Estado de trabajo para productos que requieren sublimacion'),
  ('Calado'       , 'Estado de trabajo para productos que requieren calado'),
  -- Usador para la produccion
  ('Pintado'       , 'Estado de trabajo para productos que requieren pintado'),
  ('Lijado'       , 'Estado de trabajo para productos que requieren lijado'),
  ('Cortado'      , 'Estado de trabajo para productos que requieren cortado')
)
SELECT estados_seg.estado, estados_seg.descripcion, 'system', 'system' FROM estados_seg;

-- ProductoTipo, SubTipo, Medida, Modelo, Producto
INSERT INTO producto_tipo (tipo, descripcion)
SELECT tipo, descripcion 
FROM (VALUES
  ('Tabla','Tabla de fibran'),
  ('Baner','Baner de vinil'),
  ('Calado','Calado de fibran'),
  ('Camisa','Camisa sublimada'),
  ('Taza','Taza sublimada'),
  ('Llavero','Llaveros de acrilico')
) AS valores(tipo, descripcion);


INSERT INTO producto_sub_tipo (sub_tipo, descripcion, usuario_creacion, usuario_modificacion)
SELECT sub_tipo, descripcion, 'system', 'system' 
FROM (VALUES
  ('Pintado','Trabajo que necesita pintado'),
  ('Lijado','Trabajo que necesita lijado'),
  ('Cortado','Trabajo que necesita cortado')
) AS valores(sub_tipo, descripcion);


-- ProductoTipoEstado (full workflow)
INSERT INTO producto_tipo_estado (tipo, sub_tipo, estado, secuencia, usuario_creacion, usuario_modificacion)
WITH logica_estados (tipo, sub_tipo, estado, secuencia) AS (
  VALUES

  ('Molduras' , 'Vacio'     , 'Bodega'     , 1),
  ('Molduras' , 'Vacio'     , 'Listo'      , 2),
  ('Molduras' , 'Vacio'     , 'Entregado'  , 3),

  ('Tabla'    , 'Pintado'   , 'Bodega'     , 1),
  ('Tabla'    , 'Pintado'   , 'Listo'      , 2),
  ('Tabla'    , 'Pintado'   , 'Entregado'  , 3),

  ('Tabla'    , 'Lijado'    , 'Bodega'     , 1),
  ('Tabla'    , 'Lijado'    , 'Listo'      , 2),
  ('Tabla'    , 'Lijado'    , 'Entregado'  , 3),

  ('Baner'    , 'Normal'    , 'Repartida'  , 1),
  ('Baner'    , 'Normal'    , 'Normal'     , 2),
  ('Baner'    , 'Normal'    , 'Impresion'  , 3),
  ('Baner'    , 'Normal'    , 'Armado'     , 4),
  ('Baner'    , 'Normal'    , 'Listo'      , 5),
  ('Baner'    , 'Normal'    , 'Entregado'  , 6),

  ('Baner'    , 'Reparacion', 'Repartida'  , 1),
  ('Baner'    , 'Reparacion', 'Reparacion' , 2),
  ('Baner'    , 'Reparacion', 'Impresion'  , 3),
  ('Baner'    , 'Reparacion', 'Armado'     , 4),
  ('Baner'    , 'Reparacion', 'Listo'      , 5),
  ('Baner'    , 'Reparacion', 'Entregado'  , 6),

  ('Camisa'    , 'Normal'    , 'Repartida'  , 1),
  ('Camisa'    , 'Normal'    , 'Normal'     , 2),
  ('Camisa'    , 'Normal'    , 'Impresion'  , 3),
  ('Camisa'    , 'Normal'    , 'Sublimacion', 4),
  ('Camisa'    , 'Normal'    , 'Listo'      , 5),
  ('Camisa'    , 'Normal'    , 'Entregado'  , 6),

  ('Camisa'    , 'Reparacion', 'Repartida'  , 1),
  ('Camisa'    , 'Reparacion', 'Reparacion' , 2),
  ('Camisa'    , 'Reparacion', 'Impresion'  , 3),
  ('Camisa'    , 'Reparacion', 'Sublimacion', 4),
  ('Camisa'    , 'Reparacion', 'Listo'      , 5),
  ('Camisa'    , 'Reparacion', 'Entregado'  , 6),

  ('Taza'      , 'Normal'    , 'Repartida'  , 1),
  ('Taza'      , 'Normal'    , 'Normal'     , 2),
  ('Taza'      , 'Normal'    , 'Impresion'  , 3),
  ('Taza'      , 'Normal'    , 'Sublimacion', 4),
  ('Taza'      , 'Normal'    , 'Listo'      , 5),
  ('Taza'      , 'Normal'    , 'Entregado'  , 6),

  ('Taza'      , 'Reparacion', 'Repartida'  , 1),
  ('Taza'      , 'Reparacion', 'Reparacion' , 2),
  ('Taza'      , 'Reparacion', 'Impresion'  , 3),
  ('Taza'      , 'Reparacion', 'Sublimacion', 4),
  ('Taza'      , 'Reparacion', 'Listo'      , 5),
  ('Taza'      , 'Reparacion', 'Entregado'  , 6),

  ('Llavero'   , 'Normal'    , 'Repartida'  , 1),
  ('Llavero'   , 'Normal'    , 'Normal'     , 2),
  ('Llavero'   , 'Normal'    , 'Impresion'  , 3),
  ('Llavero'   , 'Normal'    , 'Sublimacion', 4),
  ('Llavero'   , 'Normal'    , 'Listo'      , 5),
  ('Llavero'   , 'Normal'    , 'Entregado'  , 6),

  ('Llavero'   , 'Reparacion', 'Repartida'  , 1),
  ('Llavero'   , 'Reparacion', 'Reparacion' , 2),
  ('Llavero'   , 'Reparacion', 'Impresion'  , 3),
  ('Llavero'   , 'Reparacion', 'Sublimacion', 4),
  ('Llavero'   , 'Reparacion', 'Listo'      , 5),
  ('Llavero'   , 'Reparacion', 'Entregado'  , 6),

  ('Calado'    , 'Normal'    , 'Repartida'  , 1),
  ('Calado'    , 'Normal'    , 'Normal'     , 2),
  ('Calado'    , 'Normal'    , 'Impresion'  , 3),
  ('Calado'    , 'Normal'    , 'Calado'     , 4),
  ('Calado'    , 'Normal'    , 'Listo'      , 5),
  ('Calado'    , 'Normal'    , 'Entregado'  , 6),

  ('Calado'    , 'Reparacion', 'Repartida'  , 1),
  ('Calado'    , 'Reparacion', 'Reparacion' , 2),
  ('Calado'    , 'Reparacion', 'Impresion'  , 3),
  ('Calado'    , 'Reparacion', 'Calado'     , 4),
  ('Calado'    , 'Reparacion', 'Listo'      , 5),
  ('Calado'    , 'Reparacion', 'Entregado'  , 6)
)
SELECT logica_estados.tipo, logica_estados.sub_tipo, logica_estados.estado, logica_estados.secuencia, 'system', 'system' FROM logica_estados;
