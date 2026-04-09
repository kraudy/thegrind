export interface FacturaDetalle {
  idFactura: number;
  idDetalle: number;
  idOrdenDetalle: number;
  idProducto: number;
  nombreProducto: string;
  precio: number;
  cantidad: number;
  subtotal: number;
  usuarioCreacion: string;
  fechaCreacion: Date;
}