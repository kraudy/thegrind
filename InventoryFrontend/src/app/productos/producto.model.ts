export interface Producto {
  id: number;
  tipoProducto: string; // tipoProducto: ProductoTipo;
  nombre: string;
  descripcion: string;
  fechaCreacion: Date;
  fechaModificacion: Date;
  activo: boolean;
}