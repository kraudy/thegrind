
-- =============================================
-- Create orden_costo table with constraints
-- =============================================

CREATE TABLE orden_costo (
  id_orden BIGINT NOT NULL,
  id_orden_detalle BIGINT NOT NULL,
  tipo_costo VARCHAR(25) NOT NULL,
  trabajador VARCHAR(50) NOT NULL,
  rol VARCHAR(30) NOT NULL,
  id_producto BIGINT NOT NULL,
  cantidad_orden INTEGER NOT NULL CHECK (cantidad_orden > 0),
  cantidad_asignada INTEGER NOT NULL CHECK (cantidad_asignada > 0),
  cantidad_trabajada INTEGER NOT NULL DEFAULT 0 CHECK (cantidad_trabajada >= 0),
  costo NUMERIC(12,4) NOT NULL CHECK (costo >= 0),
  pagado BOOLEAN NOT NULL DEFAULT FALSE,
  usuario_paga VARCHAR(50),
  fecha_pago DATE,
  comentario VARCHAR(100) NOT NULL,
  fecha_trabajo DATE NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_creacion VARCHAR(50) NOT NULL,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_orden, id_orden_detalle, tipo_costo)
);

-- Add check constraint for tipo_costo
ALTER TABLE orden_costo
ADD CONSTRAINT chk_orden_costo_tipo_costo
CHECK (tipo_costo IN ('Reparacion', 'Pegado'));

-- Add foreign key constraints
ALTER TABLE orden_costo
ADD CONSTRAINT fk_orden_costo_orden_detalle
FOREIGN KEY (id_orden, id_orden_detalle)
REFERENCES orden_detalle(id_orden, id_orden_detalle);

ALTER TABLE orden_costo
ADD CONSTRAINT fk_orden_costo_producto
FOREIGN KEY (id_producto) REFERENCES producto(id);