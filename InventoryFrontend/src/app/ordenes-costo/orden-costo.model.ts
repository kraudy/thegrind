export interface OrdenCosto {
  idOrden: number;
  idOrdenDetalle: number;
  tipoCosto: string;
  trabajador: string;
  rol: string;
  idProducto: number;
  cantidadOrden: number;
  cantidadTrabajada: number;
  pagado: boolean;
  usuarioPaga?: string;
  fechaPago?: Date;
  comentario: string;
  fechaTrabajo: Date;
  fechaCreacion: Date;
  usuarioCreacion: string;
  fechaModificacion: Date;
}