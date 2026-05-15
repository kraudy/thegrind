

-- =============================================
-- Agrega medida a Varilla Gruesa
-- =============================================

INSERT INTO producto_medida (medida, descripcion)
SELECT medida, descripcion 
FROM (VALUES
  ('24x40','Medida 24x40')
  
) AS valores(medida, descripcion);

INSERT INTO producto_config (tipo, sub_tipo, medida, modelo, color)
SELECT
  'Molduras',
  st.sub_tipo,
  m.medida,
  mod.modelo,
  col.color
FROM (VALUES ('Normal'), ('Reparacion'), ('Vacio')) AS st(sub_tipo)
CROSS JOIN (VALUES ('24x40')) AS m(medida)
CROSS JOIN (VALUES ('VarillaGruesa')) AS mod(modelo)
CROSS JOIN (VALUES ('Nogal'), ('Cristobal'), ('Blanco'), ('Negro'), ('Natural')) AS col(color)
;