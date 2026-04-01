-- V3: Populate lookup and master data (exact copy of your provided scripts)

-- Usuarios
INSERT INTO usuario (usuario, password, usuario_creacion, usuario_modificacion)
WITH Usr (usuario, password) AS (
  VALUES
  ('mileydi.treminio', ''),
  ('suhey.vega', ''),
  ('alejandra.torres', ''),
  ('esau', ''),
  ('alistador', ''),
  ('pegador', ''),
  ('entregador', ''),
  ('repartidor', ''),
  ('tito', 'pass')
)
SELECT Usr.usuario, Usr.password, 'system', 'system' FROM Usr;

-- Roles
INSERT INTO rol (rol, descripcion, usuario_creacion, usuario_modificacion)
WITH Roles (rol, descripcion) AS (
  VALUES
  ('whatsapp', 'Recibe trabajo del whatsapp'),
  ('recibe', 'Recibe trabajo fisico'),
  ('repara', 'Realiza trabajo reparacion'),
  ('normal', 'Realiza trabajo normal'),
  ('imprime', 'Imprime trabajo'),
  ('alista', 'Alista orden para estado Listo'),
  ('entrega', 'Entrega orden en estado Listo al cliente'),
  ('reparte', 'Reparte trabajo Normal y Reparacion a repara y normal'),
  ('agenda', 'Agenda fecha de trabajo para entrar a proceso'),
  ('supervisa', 'Supervisa estado de trabajo'),
  ('factura', 'Factura orden'),
  ('inventario', ''),
  ('producto', ''),
  ('cliente', ''),
  ('precio', ''),
  ('pedido', ''),
  ('admin', 'Administrador'),
  ('pega', 'Pega retablos normales y reparacion'),
  ('lija', ''),
  ('pinta', ''),
  ('corta', ''),
  ('cala', '')
)
SELECT Roles.rol, Roles.descripcion, 'system', 'system' FROM Roles;

-- Usuario-Rol
INSERT INTO usuario_rol (usuario, rol)
WITH UsrRol (usuario, rol) AS (
  VALUES
  ('mileydi.treminio', 'repara'),
  ('suhey.vega', 'repara'),
  ('alejandra.torres', 'normal'),
  ('esau', 'pega'),
  ('pegador', 'pega'),
  ('alistador', 'alista'),
  ('entregador', 'entrega'),
  ('repartidor', 'reparte'),
  ('tito', 'admin')
)
SELECT UsrRol.usuario, UsrRol.rol FROM UsrRol;

-- Estados de seguimiento
INSERT INTO estado_seguimiento (estado, descripcion, usuario_creacion, usuario_modificacion)
WITH estados_seg (estado, descripcion) AS (
  VALUES
  ('Recibida'  , 'Estado inicial'),
  ('Repartida' , 'Estado de trabajo asigando en el calendario'),
  ('Reparacion', 'Estado inicial para trabajos que necesitan edicion'),
  ('Impresion' , 'Estado de impresion de trabajo por el controlador'),
  ('Enmarcado' , 'Estado de enmarcado de impresiones en molduras'),
  ('Pegado'    , 'Estado de pegado de impresiones en retablos'),
  ('Listo'     , 'Estado de trabajo listo, aplica para ordenes y detalle. Previo a entregar'),
  ('Entregado' , 'Estado de trabajo entregado al cliente, aplica para ordenes y detalle. Previo a facturar'),
  ('Normal'    , 'Estado inicial para trabajos normales. Solo aplica para detalle')
)
SELECT estados_seg.estado, estados_seg.descripcion, 'system', 'system' FROM estados_seg;

-- ProductoTipo, SubTipo, Medida, Modelo, Producto
INSERT INTO producto_tipo (tipo, descripcion)
SELECT tipo, descripcion 
FROM (VALUES
  ('Molduras','Molduras doradas'),
  ('Retablos','Retablos de fibran'),
  ('Ampliaciones','Ampliaciones')
) AS valores(tipo, descripcion);

INSERT INTO producto_sub_tipo (sub_tipo, descripcion, usuario_creacion, usuario_modificacion)
SELECT sub_tipo, descripcion, 'system', 'system' 
FROM (VALUES
  ('Normal','Trabajo que no necesita edicion'),
  ('Reparacion','Trabajo que necesita edicion'),
  ('Vacio','Producto que no requiere trabajo')
) AS valores(sub_tipo, descripcion);

INSERT INTO producto_medida (medida, descripcion)
SELECT medida, descripcion 
FROM (VALUES
  ('24x30','Medida 24x30'),
  ('20x24','Medida 20x24'),
  ('16x24','Medida 16x24'),
  ('16x20','Medida 16x20'),
  ('13x19','Medida 13x19'),
  ('12x18','Medida 12x18'),
  ('12x16','Medida 12x16'),
  ('11x17','Medida 11x17'),
  ('11x14','Medida 11x14'),
  ('10x15','Medida 10x15'),
  ('8.5x11','Medida 8.5x11'),
  ('8x10','Medida 8x10'),
  ('6x8','Medida 6x8'),
  ('4x6','Medida 4x6')
) AS valores(medida, descripcion);

INSERT INTO producto_modelo (modelo, descripcion)
SELECT modelo, descripcion 
FROM (VALUES
  ('Ninguno','No tiene modelo'),
  ('NG','Gruesa'),
  ('1040','')
) AS valores(modelo, descripcion);

INSERT INTO producto (nombre, descripcion, tipo_producto, sub_tipo_producto, medida_producto, modelo_producto)
SELECT nombre, descripcion, tipo_producto, sub_tipo_producto, medida_producto, modelo_producto 
FROM (VALUES
  ('Moldura Reparacion 11x17','Moldura dorada reparacion 11x17','Molduras','Reparacion','11x17','Ninguno'),
  ('Ampliacion Reparacion 11x17','Ampliacion Reparacion 11x17','Ampliaciones','Reparacion','11x17','Ninguno'),
  ('Ampliacion Normal 11x17','Ampliacion Normal 11x17','Ampliaciones','Normal','11x17','Ninguno'),
  ('Moldura Normal 11x17','Moldura dorada normal 11x17','Molduras','Normal','11x17','Ninguno'),
  ('Retablo diploma reparacion','Retablo diploma reparacion 8.5x11','Retablos','Reparacion','8.5x11','Ninguno'),
  ('Retablo diploma normal','Retablo diploma normal 8.5x11','Retablos','Normal','8.5x11','Ninguno')
) AS valores(nombre, descripcion, tipo_producto, sub_tipo_producto, medida_producto, modelo_producto);

-- ProductoTipoEstado (full workflow)
INSERT INTO producto_tipo_estado (tipo, sub_tipo, estado, secuencia, usuario_creacion, usuario_modificacion)
WITH logica_estados (tipo, sub_tipo, estado, secuencia) AS (
  VALUES
  ('Ampliaciones', 'Normal'    , 'Repartida'  , 1),
  ('Ampliaciones', 'Normal'    , 'Normal'     , 2),
  ('Ampliaciones', 'Normal'    , 'Impresion'  , 3),
  ('Ampliaciones', 'Normal'    , 'Listo'      , 4),
  ('Ampliaciones', 'Normal'    , 'Entregado'  , 5),

  ('Ampliaciones', 'Reparacion', 'Repartida'  , 1),
  ('Ampliaciones', 'Reparacion', 'Reparacion' , 2),
  ('Ampliaciones', 'Reparacion', 'Impresion'  , 3),
  ('Ampliaciones', 'Reparacion', 'Listo'      , 4),
  ('Ampliaciones', 'Reparacion', 'Entregado'  , 5),

  ('Molduras'    , 'Normal'    , 'Repartida'  , 1),
  ('Molduras'    , 'Normal'    , 'Normal'     , 2),
  ('Molduras'    , 'Normal'    , 'Impresion'  , 3),
  ('Molduras'    , 'Normal'    , 'Enmarcado'  , 4),
  ('Molduras'    , 'Normal'    , 'Listo'      , 5),
  ('Molduras'    , 'Normal'    , 'Entregado'  , 6),

  ('Molduras'    , 'Reparacion', 'Repartida'  , 1),
  ('Molduras'    , 'Reparacion', 'Reparacion' , 2),
  ('Molduras'    , 'Reparacion', 'Impresion'  , 3),
  ('Molduras'    , 'Reparacion', 'Enmarcado'  , 4),
  ('Molduras'    , 'Reparacion', 'Listo'      , 5),
  ('Molduras'    , 'Reparacion', 'Entregado'  , 6),

  ('Retablos'    , 'Normal'    , 'Repartida'  , 1),
  ('Retablos'    , 'Normal'    , 'Normal'     , 2),
  ('Retablos'    , 'Normal'    , 'Impresion'  , 3),
  ('Retablos'    , 'Normal'    , 'Pegado'     , 4),
  ('Retablos'    , 'Normal'    , 'Listo'      , 5),
  ('Retablos'    , 'Normal'    , 'Entregado'  , 6),

  ('Retablos'    , 'Reparacion', 'Repartida'  , 1),
  ('Retablos'    , 'Reparacion', 'Reparacion' , 2),
  ('Retablos'    , 'Reparacion', 'Impresion'  , 3),
  ('Retablos'    , 'Reparacion', 'Pegado'     , 4),
  ('Retablos'    , 'Reparacion', 'Listo'      , 5),
  ('Retablos'    , 'Reparacion', 'Entregado'  , 6)
)
SELECT logica_estados.tipo, logica_estados.sub_tipo, logica_estados.estado, logica_estados.secuencia, 'system', 'system' FROM logica_estados;
