
-- =============================================
-- Agregar modelos de moldura varilla y VarillaGruesa
-- =============================================

INSERT INTO producto_modelo (modelo, descripcion)
SELECT modelo, descripcion 
FROM (VALUES
  ('Varilla','Moldura varilla'),
  ('VarillaGruesa','Moldura varilla gruesa')
) AS valores(modelo, descripcion);

-- =============================================
-- Agregar color para moldura
-- =============================================

INSERT INTO producto_color (color, descripcion)
VALUES
  ('Nogal', 'Tinte nogal'),
  ('Cristobal', 'Tinte cristobal')
;

-- =============================================
-- Agrega tipo carita, vidrio y pizarra
-- =============================================

INSERT INTO producto_tipo (tipo, descripcion)
SELECT tipo, descripcion 
FROM (VALUES
  ('Carita','Carita por reparacion'),
  ('Vidrio','Vidrio para molduras'),
  ('Pizarra','Pizarras')
) AS valores(tipo, descripcion);

-- =============================================
-- Agrega moldura Varilla y VarillaGruesa, caritas, vidrios y pizarras
-- =============================================

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Molduras',
  st.sub_tipo,
  m.medida,
  mod.modelo,
  col.color
FROM (VALUES ('Normal'), ('Reparacion'), ('Vacio')) AS st(sub_tipo)
CROSS JOIN (VALUES ('24x30'), ('20x24'), ('16x24'), ('16x20'), ('13x19'), ('12x18'), ('12x16'), ('11x17'), ('11x14'), ('10x15'), ('8.5x11'), ('8x10'), ('6x8'), ('4x6')) AS m(medida)
CROSS JOIN (VALUES ('Varilla', 'VarillaGruesa')) AS mod(modelo)
CROSS JOIN (VALUES ('Nogal', 'Cristobal', 'Blanco', 'Negro', 'Natural')) AS col(color)
;

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Carita',
  st.sub_tipo,
  'Ninguno',
  'Ninguno',
  'Ninguno'
FROM (VALUES ('Reparacion')) AS st(sub_tipo)
;

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Vidrio',
  'Ninguno',
  m.medida,
  'Ninguno',
  'Ninguno'
FROM (VALUES ('24x30'), ('20x24'), ('16x24'), ('16x20'), ('13x19'), ('12x18'), ('12x16'), ('11x17'), ('11x14'), ('10x15'), ('8.5x11'), ('8x10'), ('6x8'), ('4x6')) AS m(medida)
;

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Pizarra',
  'Ninguno',
  m.medida,
  'Ninguno',
  'Ninguno'
FROM (VALUES ('Pequeña'), ('Mediana'), ('Grande')) AS m(medida)
;

-- =============================================
-- Agrega estados workflow para caritas, vidrios y pizarras
-- =============================================

INSERT INTO producto_tipo_estado (tipo, sub_tipo, estado, secuencia, usuario_creacion, usuario_modificacion)
WITH logica_estados (tipo, sub_tipo, estado, secuencia) AS (
  VALUES

  -- No es necesario ingresar estados para las molduras varilla y varilla gruesa porque ya las cubren las de las molduras

  ('Carita', 'Reparacion', 'Repartida'  , 1),
  ('Carita', 'Reparacion', 'Reparacion' , 2),
  ('Carita', 'Reparacion', 'Impresion'  , 3),
  ('Carita', 'Reparacion', 'Listo'      , 4),
  ('Carita', 'Reparacion', 'Entregado'  , 5),

  ('Vidrio'   , 'Ninguno'       , 'Bodega'     , 1),
  ('Vidrio'   , 'Ninguno'       , 'Listo'      , 2),
  ('Vidrio'   , 'Ninguno'       , 'Entregado'  , 3),

  ('Pizarra'  , 'Ninguno'       , 'Bodega'     , 1),
  ('Pizarra'  , 'Ninguno'       , 'Listo'      , 2),
  ('Pizarra'  , 'Ninguno'       , 'Entregado'  , 3)

)
SELECT logica_estados.tipo, logica_estados.sub_tipo, logica_estados.estado, logica_estados.secuencia, 'system', 'system' FROM logica_estados;
