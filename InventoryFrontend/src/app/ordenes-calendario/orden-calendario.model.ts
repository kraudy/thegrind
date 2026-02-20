
export interface OrdenCalendario {
  idOrden: number;
  fechaTrabajo: string;

  fecha: string;
  diaTrabajo: number;
  horaTrabajo: number;
  minutoTrabajo: number;

  clienteNombre: string; 
  fechaVencimiento: Date;
  
  fechaCreacion: string;
  usuarioCreacion: string;

  fechaModificacion: string;
  usuarioModificacion: string;
}