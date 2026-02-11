export interface Producto {
  id: number;

  tipoProducto: string;
  nombre: string;
  descripcion: string;
  fechaCreacion: Date;
  fechaModificacion: Date;
  activo: boolean;
}