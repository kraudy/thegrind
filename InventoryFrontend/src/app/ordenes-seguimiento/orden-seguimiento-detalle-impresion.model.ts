
export interface OrdenSeguimientoDetalleImpresion {
  idOrden: number;
  idOrdenDetalle: number;
  idProducto: number;
  nombreProducto: string;
  cantidad: number;
  tipoProducto: string;
  subTipoProducto: string;
  estadoActual: string;
  trabajador: string;
  cantidadTrabajada: number;
  cantidadPendiente: number;
  permiteMover: boolean;
}