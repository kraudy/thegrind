
-- =============================================
-- Actualizar modelo
-- =============================================

-- Cambiar nombre del modelo 'Sencilla' a 'Sencillo'
UPDATE producto_modelo
SET modelo = 'Sencillo'
WHERE modelo = 'Sencilla';

-- =============================================
-- Ingresar medidas baner
-- =============================================

INSERT INTO producto_medida (medida, descripcion)
SELECT medida, descripcion 
FROM (VALUES
  ('50x70','Medida 50x70 Baner'),
  ('60x80','Medida 60x80 Baner'),
  ('100x80','Medida 100x80 Baner'),

  ('4x3','Medida 4x3 Calado'),
  ('6x9','Medida 6x9 Calado'),

  ('Pequeña','Medida Pequeña Calado Repisa'),
  ('Mediana','Medida Mediana Calado Repisa'),
  ('Grande','Medida Grande Calado Repisa')
  
) AS valores(medida, descripcion);

-- =============================================
-- Ingresar medida Ninguno
-- =============================================

INSERT INTO producto_medida (medida, descripcion)
SELECT medida, descripcion 
FROM (VALUES
  ('Ninguno','No aplica medida')
) AS valores(medida, descripcion);

-- =============================================
-- Ingresar color blanco
-- =============================================

INSERT INTO producto_color (color, descripcion)
VALUES
  ('Blanco', 'Blanco Tazas/Camisas')
;

-- =============================================
-- Ingresar producto sub tipos
-- =============================================

INSERT INTO producto_sub_tipo (sub_tipo, descripcion, usuario_creacion, usuario_modificacion)
SELECT sub_tipo, descripcion, 'system', 'system' 
FROM (VALUES
  ('Ninguno','Sin sub-tipo')
) AS valores(sub_tipo, descripcion);

-- =============================================
-- Ingresar producto tipos
-- =============================================

INSERT INTO producto_tipo (tipo, descripcion)
SELECT tipo, descripcion 
FROM (VALUES
  ('Espejo','Espejos')
) AS valores(tipo, descripcion);

-- =============================================
-- Ingresar modelo
-- =============================================

INSERT INTO producto_modelo (modelo, descripcion)
SELECT modelo, descripcion 
FROM (VALUES
  ('Magica','Taza magica'),
  ('Normal','Taza normal'),
  -- Llavero
  ('Madera','LLavero madera'),
  ('Lamina','Llavero lamina'),
  ('Acrilico','Llavero acrilico'),
  -- Calado
  ('ConBase','Calado ConBase'),
  ('SinBase','Calado SinBase'),
  ('Repisa','Calado Repisa'),

  ('1002','Modelo de moldura 1002'),
  ('1013','Modelo de moldura 1013'),
  -- Ampliaciones
  ('Fotografica','Ampliacion papel fotografico')

) AS valores(modelo, descripcion);