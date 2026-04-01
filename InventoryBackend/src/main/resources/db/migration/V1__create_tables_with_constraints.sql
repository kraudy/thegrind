-- V1: Create all tables + PKs, indexes, CHECK constraints, defaults where defined in entities

CREATE TABLE cliente (
  id BIGSERIAL PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  apellido VARCHAR(100) NOT NULL,
  telefono VARCHAR(15) NOT NULL,
  correo VARCHAR(100) NOT NULL,
  direccion VARCHAR(255) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL,
  fecha_modificacion TIMESTAMP NOT NULL
);

CREATE TABLE estado_seguimiento (
  estado VARCHAR(25) PRIMARY KEY,
  descripcion VARCHAR(255) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_creacion VARCHAR(100) NOT NULL,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion VARCHAR(100) NOT NULL
);

CREATE TABLE orden (
  id BIGSERIAL PRIMARY KEY,
  id_cliente BIGINT NOT NULL,
  creada_por VARCHAR(50) NOT NULL,
  modificada_por VARCHAR(50),
  total_monto NUMERIC(12,4) NOT NULL DEFAULT 0,
  total_productos INTEGER NOT NULL DEFAULT 0,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_vencimiento TIMESTAMP NOT NULL,
  fecha_preparada TIMESTAMP,
  fecha_despachada TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado VARCHAR(25) NOT NULL DEFAULT 'Recibida'
);

CREATE TABLE orden_calendario (
  id_orden BIGINT PRIMARY KEY,
  fecha_trabajo TIMESTAMP NOT NULL,
  fecha DATE NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_creacion VARCHAR(100) NOT NULL,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion VARCHAR(100) NOT NULL
);
CREATE INDEX idx_fecha ON orden_calendario (fecha);

CREATE TABLE orden_calendario_historico (
  id BIGSERIAL PRIMARY KEY,
  id_orden BIGINT NOT NULL,
  fecha_trabajo_original TIMESTAMP NOT NULL,
  fecha_original DATE NOT NULL,
  fecha_archivado TIMESTAMP NOT NULL,
  usuario_archivado VARCHAR(100) NOT NULL DEFAULT 'system'
);

CREATE TABLE orden_detalle (
  id_orden BIGINT NOT NULL,
  id_orden_detalle BIGINT NOT NULL,
  id_producto BIGINT NOT NULL,
  cantidad INTEGER NOT NULL,
  precio_unitario NUMERIC(12,4) NOT NULL,
  subtotal NUMERIC(12,4) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_orden, id_orden_detalle)
);

CREATE TABLE orden_pago (
  id BIGSERIAL PRIMARY KEY,
  id_orden BIGINT NOT NULL,
  monto NUMERIC(12,4) NOT NULL,
  fecha_pago TIMESTAMP NOT NULL,
  metodo_pago VARCHAR(30) NOT NULL,
  codigo_referencia VARCHAR(30),
  banco VARCHAR(20),
  estado VARCHAR(20) NOT NULL DEFAULT 'Pendiente',
  recibido_por VARCHAR(50),
  aprobado_por VARCHAR(50),
  fecha_aprobacion TIMESTAMP,
  notas TEXT
);

CREATE TABLE orden_seguimiento (
  id_orden BIGINT NOT NULL,
  id_orden_detalle BIGINT NOT NULL,
  tipo VARCHAR(25) NOT NULL,
  sub_tipo VARCHAR(25) NOT NULL,
  secuencia INTEGER NOT NULL CHECK (secuencia > 0),
  estado VARCHAR(100) NOT NULL,
  seguimiento_por VARCHAR(50) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_orden, id_orden_detalle)
);

CREATE TABLE orden_seguimiento_historico (
  id BIGSERIAL PRIMARY KEY,
  id_orden BIGINT NOT NULL,
  id_orden_detalle BIGINT NOT NULL,
  estado VARCHAR(100) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_creacion VARCHAR(50) NOT NULL,
  fecha_finalizacion TIMESTAMP NOT NULL,
  usuario_finalizacion VARCHAR(50) NOT NULL,
  duracion BIGINT NOT NULL,
  fecha_registro TIMESTAMP NOT NULL
);
CREATE INDEX idx_id_orden ON orden_seguimiento_historico (id_orden);
CREATE INDEX idx_estado ON orden_seguimiento_historico (estado);

CREATE TABLE orden_trabajo (
  id_orden BIGINT NOT NULL,
  id_orden_detalle BIGINT NOT NULL,
  estado VARCHAR(25) NOT NULL,
  secuencia INTEGER NOT NULL CHECK (secuencia > 0),
  trabajador VARCHAR(50) NOT NULL,
  rol VARCHAR(30) NOT NULL,
  id_producto BIGINT NOT NULL,  
  cantidad_asignada INTEGER NOT NULL,
  cantidad_trabajada INTEGER NOT NULL,
  cantidad_no_trabajada INTEGER NOT NULL,
  comentario VARCHAR(100) NOT NULL,
  fecha_trabajo DATE NOT NULL,
  id_seguimiento BIGINT,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_orden, id_orden_detalle, estado)
);

CREATE TABLE producto (
  id BIGSERIAL PRIMARY KEY,
  tipo_producto VARCHAR(25) NOT NULL,
  sub_tipo_producto VARCHAR(25) NOT NULL,
  medida_producto VARCHAR(25) NOT NULL,
  modelo_producto VARCHAR(25) NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  activo BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE producto_medida (
  medida VARCHAR(25) PRIMARY KEY,
  descripcion VARCHAR(255) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE producto_modelo (
  modelo VARCHAR(25) PRIMARY KEY,
  descripcion VARCHAR(255) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE producto_precio (
  producto_id BIGINT NOT NULL,
  precio NUMERIC(12,4) NOT NULL,
  descripcion VARCHAR(255) NOT NULL,
  cantidad_requerida INTEGER NOT NULL DEFAULT 0,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  activo BOOLEAN NOT NULL DEFAULT true,
  PRIMARY KEY (producto_id, precio)
);

CREATE TABLE producto_sub_tipo (
  sub_tipo VARCHAR(25) PRIMARY KEY,
  descripcion VARCHAR(255) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_creacion VARCHAR(100) NOT NULL,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion VARCHAR(100) NOT NULL
);

CREATE TABLE producto_tipo (
  tipo VARCHAR(25) PRIMARY KEY,
  descripcion VARCHAR(255) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE producto_tipo_estado (
  tipo VARCHAR(25) NOT NULL,
  sub_tipo VARCHAR(25) NOT NULL,
  secuencia INTEGER NOT NULL CHECK (secuencia > 0),
  estado VARCHAR(25) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_creacion VARCHAR(100) NOT NULL,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion VARCHAR(100) NOT NULL,
  PRIMARY KEY (tipo, sub_tipo, secuencia)
);

CREATE TABLE rol (
  rol VARCHAR(30) PRIMARY KEY,
  descripcion VARCHAR(255),
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_creacion VARCHAR(100) NOT NULL DEFAULT 'system',
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion VARCHAR(100) NOT NULL DEFAULT 'system',
  activo BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE usuario (
  usuario VARCHAR(50) PRIMARY KEY,
  password VARCHAR(255),
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_creacion VARCHAR(100) NOT NULL DEFAULT 'system',
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion VARCHAR(100) NOT NULL DEFAULT 'system',
  activo BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE usuario_rol (
  usuario VARCHAR(50) NOT NULL,
  rol VARCHAR(30) NOT NULL,
  PRIMARY KEY (usuario, rol)
);