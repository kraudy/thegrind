export interface OrdenEstadisticas {
  ordenesRecibidas: 0,

  reparadores: TrabajadorEstadisticas;
  normales: TrabajadorEstadisticas;
  repartidas: TrabajadorEstadisticas;
  
  impresionNormal: 0;
  impresionReparacion: 0;
  bodega: 0;
  armado: 0;
  calado: 0;
  pegado: 0;
  enmarcado: 0;
  alistado: 0
}

export interface TrabajadorEstadisticas {
  trabajador: string;
  cantidadDetalles: number;
}