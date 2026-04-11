CREATE TABLE producto_color (
  color VARCHAR(25) PRIMARY KEY,
  descripcion VARCHAR(255) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO producto_color (color, descripcion)
VALUES
  ('Dorada', 'Moldura dorada'),
  ('Natural', 'Retablo/Tabla estilo madera natural'),
  ('Negro', 'Retablo/Tabla negro mate'),
  ('Rojo', 'Retablo/Tabla roja'),
  ('Ninguno', 'Sin color')
;

ALTER TABLE producto
  ADD COLUMN color_producto VARCHAR(25) NOT NULL DEFAULT 'Ninguno';

-- update existing rows:
UPDATE producto SET color_producto = 'Dorada'
WHERE tipo_producto = 'Molduras';

UPDATE producto SET color_producto = 'Rojo'
WHERE tipo_producto = 'Retablos';


ALTER TABLE producto
  ADD CONSTRAINT fk_producto_color 
  FOREIGN KEY (color_producto) REFERENCES producto_color(color);


INSERT INTO producto_modelo (modelo, descripcion)
SELECT modelo, descripcion 
FROM (VALUES
  ('Sencilla','Retablo/Tabla sencilla'),
  ('Especial','Retablo/Tabla especial'),
  ('Mariposa','Retablo/Tabla mariposa')
) AS valores(modelo, descripcion);