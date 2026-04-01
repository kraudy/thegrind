-- V2: All foreign-key constraints (referential integrity)

ALTER TABLE orden 
  ADD CONSTRAINT fk_orden_cliente 
  FOREIGN KEY (id_cliente) REFERENCES cliente(id);

ALTER TABLE orden 
  ADD CONSTRAINT fk_orden_estado_seguimiento 
  FOREIGN KEY (estado) REFERENCES estado_seguimiento(estado);

ALTER TABLE orden_detalle 
  ADD CONSTRAINT fk_orden_detalle_orden 
  FOREIGN KEY (id_orden) REFERENCES orden(id);

ALTER TABLE orden_detalle 
  ADD CONSTRAINT fk_orden_detalle_producto 
  FOREIGN KEY (id_producto) REFERENCES producto(id);

ALTER TABLE orden_pago 
  ADD CONSTRAINT fk_orden_pago_orden 
  FOREIGN KEY (id_orden) REFERENCES orden(id);

ALTER TABLE orden_seguimiento 
  ADD CONSTRAINT fk_orden_seguimiento_orden_detalle 
  FOREIGN KEY (id_orden, id_orden_detalle) 
  REFERENCES orden_detalle(id_orden, id_orden_detalle);

ALTER TABLE orden_trabajo 
  ADD CONSTRAINT fk_orden_trabajo_orden_detalle 
  FOREIGN KEY (id_orden, id_orden_detalle) 
  REFERENCES orden_detalle(id_orden, id_orden_detalle);

ALTER TABLE orden_trabajo 
  ADD CONSTRAINT fk_orden_trabajo_estado_seguimiento 
  FOREIGN KEY (estado) REFERENCES estado_seguimiento(estado);

ALTER TABLE orden_calendario 
  ADD CONSTRAINT fk_orden_calendario_orden 
  FOREIGN KEY (id_orden) REFERENCES orden(id);

ALTER TABLE producto 
  ADD CONSTRAINT fk_producto_tipo 
  FOREIGN KEY (tipo_producto) REFERENCES producto_tipo(tipo);

ALTER TABLE producto 
  ADD CONSTRAINT fk_producto_sub_tipo 
  FOREIGN KEY (sub_tipo_producto) REFERENCES producto_sub_tipo(sub_tipo);

ALTER TABLE producto 
  ADD CONSTRAINT fk_producto_medida 
  FOREIGN KEY (medida_producto) REFERENCES producto_medida(medida);

ALTER TABLE producto 
  ADD CONSTRAINT fk_producto_modelo 
  FOREIGN KEY (modelo_producto) REFERENCES producto_modelo(modelo);

ALTER TABLE producto_precio 
  ADD CONSTRAINT fk_producto_precio_producto 
  FOREIGN KEY (producto_id) REFERENCES producto(id);

ALTER TABLE producto_tipo_estado 
  ADD CONSTRAINT fk_producto_tipo_estado_tipo 
  FOREIGN KEY (tipo) REFERENCES producto_tipo(tipo);

ALTER TABLE producto_tipo_estado 
  ADD CONSTRAINT fk_producto_tipo_estado_sub_tipo 
  FOREIGN KEY (sub_tipo) REFERENCES producto_sub_tipo(sub_tipo);

ALTER TABLE producto_tipo_estado 
  ADD CONSTRAINT fk_producto_tipo_estado_estado 
  FOREIGN KEY (estado) REFERENCES estado_seguimiento(estado);

ALTER TABLE usuario_rol 
  ADD CONSTRAINT fk_usuario_rol_usuario 
  FOREIGN KEY (usuario) REFERENCES usuario(usuario);

ALTER TABLE usuario_rol 
  ADD CONSTRAINT fk_usuario_rol_rol 
  FOREIGN KEY (rol) REFERENCES rol(rol);