
export interface OrdenSeguimientoDetalle {
  idOrden: number;
  idOrdenDetalle: number;
  idProducto: number;
  nombreProducto: string;
  cantidad: number;
  tipoProducto: string;
  subTipoProducto: string;
  estadoActual: string;
  permiteMover: boolean;
}