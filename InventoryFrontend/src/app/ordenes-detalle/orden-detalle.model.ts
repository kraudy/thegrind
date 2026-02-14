export interface OrdenDetalle {
  idOrden: number;
  idOrdenDetalle: number;
  idProducto: number;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  fechaCreacion: Date;
  fechaModificacion: Date;
}