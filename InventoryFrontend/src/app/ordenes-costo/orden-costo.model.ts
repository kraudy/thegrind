export interface OrdenCosto {
  idOrden: number;
  idOrdenDetalle: number;
  tipoCosto: string;
  trabajador: string;
  rol: string;
  idClinete: number;
  clienteNombre: string;
  idProducto: number;
  cantidadOrden: number;
  cantidadAsignada: number;
  cantidadTrabajada: number;
  costo: number;
  subTotal: number;
  pagado: boolean;
  usuarioPaga?: string;
  fechaPago?: Date;
  comentario: string;
  fechaTrabajo: Date;
  fechaCreacion: Date;
  usuarioCreacion: string;
  fechaModificacion: Date;
}