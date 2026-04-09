-- Create Factura table
CREATE TABLE factura (
  id BIGSERIAL PRIMARY KEY,
  id_cliente BIGINT NOT NULL,
  id_orden BIGINT NOT NULL,
  total NUMERIC(12,4) NOT NULL,
  usuario_creacion VARCHAR(50) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado VARCHAR(25) NOT NULL,

  CONSTRAINT chk_estado CHECK (estado IN ('Pagada', 'Anulada', 'Parcial')),
  CONSTRAINT fk_factura_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id),
  CONSTRAINT fk_factura_orden   FOREIGN KEY (id_orden) REFERENCES orden(id),
  CONSTRAINT fk_factura_usuario FOREIGN KEY (usuario_creacion) REFERENCES usuario(usuario)
);

-- Create FacturaDetalle table
CREATE TABLE factura_detalle (
  id_factura BIGINT NOT NULL,
  id_detalle BIGINT NOT NULL,
  id_orden_detalle BIGINT NOT NULL,
  id_producto BIGINT NOT NULL,
  precio DECIMAL(12,4) NOT NULL,
  cantidad INTEGER NOT NULL,
  subtotal DECIMAL(12,4) NOT NULL,
  usuario_creacion VARCHAR(50) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id_factura, id_detalle),
  CONSTRAINT fk_factura_detalle_factura   FOREIGN KEY (id_factura) REFERENCES factura(id),
  CONSTRAINT fk_factura_detalle_producto  FOREIGN KEY (id_producto) REFERENCES producto(id),
  CONSTRAINT fk_factura_detalle_usuario   FOREIGN KEY (usuario_creacion) REFERENCES usuario(usuario)
);
