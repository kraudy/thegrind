export interface Factura {
  id: number;
  idCliente: number;
  clienteNombre: string;
  idOrden: number;
  usuarioCreacion: string;
  total: number;
  fechaCreacion: Date;
  estado: string;
}