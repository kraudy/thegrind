export interface ProductoPrecio {
  productoId: number;
  precio: number;
  descripcion: string;
  cantidadRequerida: number;
  fechaCreacion: Date;
  fechaModificacion: Date;
  activo: boolean;
}