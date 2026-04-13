
-- =============================================
-- Agrega medida para espejo
-- =============================================

INSERT INTO producto_medida (medida, descripcion)
SELECT medida, descripcion 
FROM (VALUES
  ('14x30','Medida 14x30')
) AS valores(medida, descripcion);

-- =============================================
-- Agrega combinaciones validas
-- =============================================

TRUNCATE TABLE producto_config;   -- optional: start fresh

-- 1. Ampliaciones (todas las medidas, Normal + Reparacion, always Ninguno)
INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Ampliaciones',
  st.sub_tipo,
  m.medida,
  mod.modelo,
  'Ninguno'
FROM (VALUES ('Normal'), ('Reparacion')) AS st(sub_tipo)
CROSS JOIN (VALUES ('24x30'), ('20x24'), ('16x24'), ('16x20'), ('13x19'), ('12x18'), ('12x16'), ('11x17'), ('11x14'), ('10x15'), ('8.5x11'), ('8x10'), ('6x8'), ('4x6')) AS m(medida)
CROSS JOIN (VALUES ('Ninguno'), ('Fotografica')) AS mod(modelo)
;

-- 2. Molduras (todas las medidas, Normal | Reparacion | Vacio, Dorada)
INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Molduras',
  st.sub_tipo,
  m.medida,
  mod.modelo,
  'Dorada'
FROM (VALUES ('Normal'), ('Reparacion'), ('Vacio')) AS st(sub_tipo)
CROSS JOIN (VALUES ('11x17')) AS m(medida)
CROSS JOIN (VALUES ('NG')) AS mod(modelo)
;

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Molduras',
  st.sub_tipo,
  m.medida,
  mod.modelo,
  'Dorada'
FROM (VALUES ('Normal'), ('Reparacion'), ('Vacio')) AS st(sub_tipo)
CROSS JOIN (VALUES ('12x16'), ('11x17'), ('11x14'), ('10x15'), ('8.5x11'), ('8x10')) AS m(medida)
CROSS JOIN (VALUES ('1040')) AS mod(modelo)
;

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Molduras',
  st.sub_tipo,
  m.medida,
  mod.modelo,
  'Dorada'
FROM (VALUES ('Normal'), ('Reparacion'), ('Vacio')) AS st(sub_tipo)
CROSS JOIN (VALUES ('16x20'), ('13x19'), ('12x18')) AS m(medida)
CROSS JOIN (VALUES ('1002')) AS mod(modelo)
;

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Molduras',
  st.sub_tipo,
  m.medida,
  mod.modelo,
  'Dorada'
FROM (VALUES ('Normal'), ('Reparacion'), ('Vacio')) AS st(sub_tipo)
CROSS JOIN (VALUES ('24x30'), ('20x24'), ('16x20'), ('13x19'), ('12x18')) AS m(medida)
CROSS JOIN (VALUES ('1013')) AS mod(modelo)
;

-- 3. Retablos (all 3 sub_tipos, todas las medidas, all 3 modelos, 3 colors)
INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Retablos',
  st.sub_tipo,
  m.medida,
  mod.modelo,
  c.color
FROM (VALUES ('Normal'), ('Reparacion')) AS st(sub_tipo)
CROSS JOIN (VALUES ('24x30'), ('20x24'), ('16x24'), ('16x20'), ('13x19'), ('12x18'), ('12x16'), ('11x17'), ('11x14'), ('10x15'), ('8.5x11'), ('8x10'), ('6x8'), ('4x6')) AS m(medida)
CROSS JOIN (VALUES ('Sencillo'), ('Especial'), ('Mariposa')) AS mod(modelo)
CROSS JOIN (VALUES ('Rojo'), ('Negro'), ('Natural')) AS c(color);

-- 4. Tabla Pintado (same as Retablos but only Pintado sub_tipo)
INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Tabla',
  'Pintado',
  m.medida,
  mod.modelo,
  c.color
FROM (VALUES ('24x30'), ('20x24'), ('16x24'), ('16x20'), ('13x19'), ('12x18'), ('12x16'), ('11x17'), ('11x14'), ('10x15'), ('8.5x11'), ('8x10'), ('6x8'), ('4x6')) AS m(medida)
CROSS JOIN (VALUES ('Sencillo'), ('Especial'), ('Mariposa')) AS mod(modelo)
CROSS JOIN (VALUES ('Rojo'), ('Negro'), ('Natural')) AS c(color);

-- 5. Tabla Lijado (only Lijado sub_tipo, color always Ninguno)
INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Tabla',
  'Lijado',
  m.medida,
  mod.modelo,
  'Ninguno'
FROM (VALUES ('24x30'), ('20x24'), ('16x24'), ('16x20'), ('13x19'), ('12x18'), ('12x16'), ('11x17'), ('11x14'), ('10x15'), ('8.5x11'), ('8x10'), ('6x8'), ('4x6')) AS m(medida)
CROSS JOIN (VALUES ('Sencillo'), ('Especial'), ('Mariposa')) AS mod(modelo);

-- 6. Banner (Baner, [Normal, Reparacion], [24x30:4x6], Ninguno, Ninguno)
INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Baner',
  st.sub_tipo,
  m.medida,
  'Ninguno',
  'Ninguno'
FROM (VALUES ('Normal'), ('Reparacion')) AS st(sub_tipo)
CROSS JOIN (VALUES ('50x70'), ('60x80'), ('100x80')) AS m(medida);

-- 7. Camisa
INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Camisa',
  st.sub_tipo,
  'Ninguno',
  'Ninguno',
  'Blanco'
FROM (VALUES ('Normal'), ('Reparacion')) AS st(sub_tipo);

-- 7. Taza
INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Taza',
  st.sub_tipo,
  'Ninguno',
  'Ninguno',
  'Blanco'
FROM (VALUES ('Normal'), ('Reparacion')) AS st(sub_tipo);

-- 8. Llavero
INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Llavero',
  st.sub_tipo,
  'Ninguno',
  m.modelo,
  'Ninguno'
FROM (VALUES ('Normal'), ('Reparacion')) AS st(sub_tipo)
CROSS JOIN (VALUES ('Madera'),('Lamina'),('Acrilico')) AS m(modelo);

-- 9. Calado
INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Calado',
  'Ninguno',
  m.medida,
  mod.modelo,
  'Ninguno'
FROM (VALUES ('4x3'), ('4x6'), ('6x9')) AS m(medida)
CROSS JOIN (VALUES ('ConBase'),('SinBase')) AS mod(modelo);

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Calado',
  'Ninguno',
  m.medida,
  mod.modelo,
  'Ninguno'
FROM (VALUES ('Pequeña'), ('Mediana'), ('Grande')) AS m(medida)
CROSS JOIN (VALUES ('Repisa')) AS mod(modelo);

-- 10. Espejo
INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Espejo',
  'Ninguno',
  m.medida,
  mod.modelo,
  'Ninguno'
FROM (VALUES ('14x30')) AS m(medida)
CROSS JOIN (VALUES ('1002')) AS mod(modelo);
