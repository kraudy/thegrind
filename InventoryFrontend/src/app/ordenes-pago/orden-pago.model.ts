export interface OrdenPago {
  id: number;
  idOrden: number;
  clienteNombre: string;
  totalMonto: number;
  monto: number;
  fechaPago: Date;
  metodoPago: string;
  codigoReferencia: string;
  banco: string;
  estado: string;
  recibidoPor: string;
  aprobadoPor: string;
  fechaAprobacion: Date;
  notas: string;
  tipoPago: string;
}