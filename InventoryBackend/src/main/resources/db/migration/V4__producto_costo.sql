

CREATE TABLE producto_costo (
  producto_id BIGINT NOT NULL,
  tipo_costo VARCHAR(25) NOT NULL CHECK (tipo_costo IN ('General', 'Compra', 'Reparacion', 'Pegado', 'Fallado')),
  costo NUMERIC(12,4) NOT NULL,
  descripcion VARCHAR(255) NOT NULL DEFAULT '', 
  cantidad_requerida INTEGER NOT NULL DEFAULT 0,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  activo BOOLEAN NOT NULL DEFAULT true,
  PRIMARY KEY (producto_id, tipo_costo)
);

ALTER TABLE producto_costo 
  ADD CONSTRAINT fk_producto_costo_producto 
  FOREIGN KEY (producto_id) REFERENCES producto(id);