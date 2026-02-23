
export interface OrdenSeguimiento {
  idOrden: number;
  idOrdenDetalle: number;
  idProducto: number;
  nombreProducto: string;
  cantidad: number;
  tipoProducto: string;
  subTipoProducto: string;
  estadoActual: string;
}