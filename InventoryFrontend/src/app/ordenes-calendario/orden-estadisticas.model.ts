export interface OrdenEstadisticas {
  ordenesRecibidas: number;

  reparadores: TrabajadorEstadisticas[];
  normales: TrabajadorEstadisticas[];
  repartidas: TrabajadorEstadisticas[];

  impresionNormal: number;
  impresionReparacion: number;
  bodega: number;
  armado: number;
  calado: number;
  pegado: number;
  enmarcado: number;
  alistado: number;
}

export interface TrabajadorEstadisticas {
  trabajador: string;
  cantidadDetalles: number;
}