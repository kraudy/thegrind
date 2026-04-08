export interface OrdenFacturacion {
  id: number;
  idOrden: number;
  idCliente: number;
  clienteNombre: string;
  creadaPor: string;

  totalMontoOrden: number;
  totalMontoFactura: number;
  saldoPendiente: number;

  totalProductosOrden: number;
  totalProductosFactura: number;

  fechaCreacion: Date;
  fechaVencimiento: Date; 
  fechaLista: Date;        // fecha_preparada
  fechaEntregada: Date;    // fecha_despachada
  fechaModificacion: Date;
  estado: string;
  tiempoRestante: string;
  duracionTrabajo: string;
}