
export interface OrdenSeguimiento {
  id: number;
  idCliente: number;
  clienteNombre: string; 
  creadaPor: string;
  fechaVencimiento: Date;  // Fecha estimada de entrega, no necesariamente la fecha real de despacho
  tiempoRestante: string; // Tiempo restante hasta la fecha de vencimiento, calculado en el backend
}