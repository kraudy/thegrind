/**
 * Modelo unificado para la pantalla de Entrega y Facturacion.
 * Lista ordenes en estado Listo (por entregar) o Entregado (por facturar)
 * con sus totales de facturacion calculados en backend.
 */
export interface OrdenEntregaFacturacion {
  id: number;
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
  estado: string;          // 'Listo' | 'Entregado'
  tiempoRestante: string;
  duracionTrabajo: string;
  canal?: string;          // 'General' | 'Whatsapp'
  prioridad?: 'Normal' | 'Alta' | 'Urgente';
}
