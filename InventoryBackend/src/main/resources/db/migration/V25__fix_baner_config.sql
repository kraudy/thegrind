
-- =============================================
-- Fix baner medidas
-- =============================================

DELETE from producto_config  where tipo = 'Baner';

DELETE from producto_medida where medida = '100x80';

INSERT INTO producto_medida (medida, descripcion)
SELECT medida, descripcion 
FROM (VALUES
  ('80x100','Medida 80x100 Baner'),
  ('90x60','Medida 90x60 Baner'),
  ('70x90','Medida 70x90 Baner'),
  ('85x200','Medida 85x200 Baner')
) AS valores(medida, descripcion);


INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT 
  'Baner',
  st.sub_tipo,
  m.medida,
  'Ninguno',
  'Ninguno'
FROM (VALUES ('Normal'), ('Reparacion')) AS st(sub_tipo)
CROSS JOIN (VALUES ('50x70'), ('60x80'), ('80x100'), ('90x60'), ('70x90'), ('85x200')) AS m(medida);