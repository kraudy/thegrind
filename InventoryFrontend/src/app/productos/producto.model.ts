export interface ProductoPrecioMini {
  precio: number;
  descripcion: string;
  cantidadRequerida: number;
}

export interface ProductoCostoMini {
  tipoCosto: string;
  costo: number;
}

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
  // Embedded by GET /api/productos for the catalog list view.
  precios?: ProductoPrecioMini[];
  costos?: ProductoCostoMini[];
}