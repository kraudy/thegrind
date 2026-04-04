export interface ProductoCosto {
  productoId: number;
  tipoCosto: string;
  costo: number;
  descripcion: string;
  cantidadRequerida: number;
  fechaCreacion: Date;
  fechaModificacion: Date;
  activo: boolean;
}