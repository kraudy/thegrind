export interface Producto {
  id: number;
  tipoProducto: string;
  subTipoProducto: string; 
  medidaProducto: string;
  modeloProducto: string;
  colorProducto: string;
  nombre: string;
  descripcion: string;
  fechaCreacion: Date;
  fechaModificacion: Date;
  usuarioCreacion: string;
  usuarioModificacion: string;
  imagen: string;
  activo: boolean;
}