
export interface OrdenSeguimientoDetallePreparacion {
  idOrden: number;
  idOrdenDetalle: number;
  idProducto: number;
  nombreProducto: string;
  cantidad: number;
  tipoProducto: string;
  subTipoProducto: string;
  estadoActual: string;

  trabajadorActual: string;
  cantidadAsignadaActual: number;
  cantidadTrabajadaActual: number;

  estadoPrevio: string;
  trabajadorPrevio: string;
  cantidadAsignadaPrevio: number;
  cantidadTrabajadaPrevio: number;

  permiteMover: boolean;
}