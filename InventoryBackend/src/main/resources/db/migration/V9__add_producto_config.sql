
-- Combinaciones validas de atributos para productos
CREATE TABLE producto_config (
  tipo          VARCHAR(25) NOT NULL,
  sub_tipo      VARCHAR(25) NOT NULL,
  medida        VARCHAR(25) NOT NULL,
  modelo        VARCHAR(25) NOT NULL DEFAULT 'Ninguno',
  color         VARCHAR(25) NOT NULL DEFAULT 'Ninguno',
  
  PRIMARY KEY (tipo, sub_tipo, medida, modelo, color),
  
  CONSTRAINT fk_config_tipo      FOREIGN KEY (tipo)      REFERENCES producto_tipo(tipo),
  CONSTRAINT fk_config_sub_tipo  FOREIGN KEY (sub_tipo)  REFERENCES producto_sub_tipo(sub_tipo),
  CONSTRAINT fk_config_medida    FOREIGN KEY (medida)    REFERENCES producto_medida(medida),
  CONSTRAINT fk_config_modelo    FOREIGN KEY (modelo)    REFERENCES producto_modelo(modelo),
  CONSTRAINT fk_config_color     FOREIGN KEY (color)     REFERENCES producto_color(color)
);