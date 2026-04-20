
export interface OrdenSeguimientoDetalleGeneral {
  id: number;
  idCliente: number;
  clienteNombre: string;
  estadoOrden: string;
  creadaPor: string;
  fechaVencimiento: Date;
  tiempoRestante: string;

  tieneRepartidas: boolean;
  tieneNormales: boolean;
  tieneReparacion: boolean;
  tieneImpresion: boolean;
  tieneBodega: boolean;
  tieneArmado: boolean;
  tieneCalado: boolean;
  tieneSublimacion: boolean;
  tieneEnmarcado: boolean;
  tienePegado: boolean;
  tieneListo: boolean;
  tieneEntregado: boolean;

  countRepartidas: number;
  countNormales: number;
  countReparacion: number;
  countImpresion: number;
  countBodega: number;
  countArmado: number;
  countCalado: number;
  countSublimacion: number;
  countEnmarcado: number;
  countPegado: number;
  countListo: number;
  countEntregado: number;
}