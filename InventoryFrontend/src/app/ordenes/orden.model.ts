export interface Orden {
  id?: number;
  idCliente: number;
  recibidaPor: string;
  preparadaPor: string;
  despachadaPor: string;
  totalMonto: number;
  totalProductos: number;
  fechaCreacion: Date | null; // Fecha de creacion de la orden
  fechaEntrega: Date | null;  // Fecha estimada de entrega, no necesariamente la fecha real de despacho
  fechaPreparada: Date | null; // Fecha en que la orden fue preparada
  fechaDespachada: Date | null; // Fecha en que la orden fue despachada
  fechaModificacion: Date | null; // Fecha en que la orden fue modificada
  estado: string;
}