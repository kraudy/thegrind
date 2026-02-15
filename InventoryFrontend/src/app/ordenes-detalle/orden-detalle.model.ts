export interface OrdenDetalle {
  idOrden: number;
  idOrdenDetalle: number;
  idProducto: number;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  fechaCreacion: Date;
  fechaModificacion: Date;
}