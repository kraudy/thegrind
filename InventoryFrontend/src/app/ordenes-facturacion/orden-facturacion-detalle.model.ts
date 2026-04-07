export interface OrdenFacturacionDetalle {
  idOrden: number;
  idOrdenDetalle: number;
  idProducto: number;
  nombreProducto: string;
  cantidadOrden: number;
  subTotalOrden: number;
  precioUnitario: number;
  fechaCreacion: Date;
  fechaModificacion: Date;
  tipoProducto: string;
  subTipoProducto: string;
  trabajadorEntrega: string;
  cantidadFactura: number;
  subtotalFactura: number;
}