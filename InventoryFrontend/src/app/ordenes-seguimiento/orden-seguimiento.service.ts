import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { OrdenSeguimientoDetalle } from './orden-seguimiento-detalle.model';
import { OrdenSeguimientoDetalleImpresion } from './orden-seguimiento-detalle-impresion.model';
import { ProductoTipoEstado } from '../productos-tipo-estados/producto-tipo-estado.model';
import { OrdenSeguimiento } from './orden-seguimiento.model';
import { EstadosPorDetalleDTO } from './estados-por-detalle.model';
import { Usuario } from '../usuarios/usuario.model';
import { OrdenSeguimientoDetallePreparacion } from './orden-seguimiento-detalle-preparacion.model';
import { OrdenSeguimientoDetalleEntrega } from './orden-seguimiento-detalle-entrega.model';
import { OrdenSeguimientoDetalleGeneral } from './orden-seguimiento-detalle-general.model';

import { OrdenFacturacion } from '../ordenes-facturacion/orden-facturacion.model';
import { OrdenFacturacionDetalle } from '../ordenes-facturacion/orden-facturacion-detalle.model';


@Injectable({
  providedIn: 'root',
})
export class OrdenSeguimientoService {
  private apiUrl = '/api/ordenes-seguimiento';

  constructor(private http: HttpClient) {}

  getOrdenesSeguimientoGeneral(search: string = '', estadoOrden: string = ''): Observable<OrdenSeguimientoDetalleGeneral[]> {
    let params = new HttpParams();

    // Only send search if user actually typed something
    if (search?.trim()) {
      params = params.set('search', search.trim());
    }

    // Only send estadoOrden if a real filter is selected
    if (estadoOrden) {
      params = params.set('estadoOrden', estadoOrden);
    }

    return this.http.get<OrdenSeguimientoDetalleGeneral[]>(`${this.apiUrl}/general`, { params });
  }

  /* Seguimiento para impresion */
  getOrdenesParaImpresion(): Observable<OrdenSeguimiento[]> {
    return this.http.get<OrdenSeguimiento[]>(`${this.apiUrl}/para-impresion`);
  }

  getOrdenDetalleParaImpresion(idOrden: number): Observable<OrdenSeguimientoDetalleImpresion[]> {
    return this.http.get<OrdenSeguimientoDetalleImpresion[]>(`${this.apiUrl}/para-impresion/${idOrden}`);
  }

  /* Seguimiento para preparacion */
  getOrdenesParaPreparacion(): Observable<OrdenSeguimiento[]> {
    return this.http.get<OrdenSeguimiento[]>(`${this.apiUrl}/para-preparacion`);
  }

  getOrdenDetalleParaPreparacion(idOrden: number): Observable<OrdenSeguimientoDetallePreparacion[]> {
    return this.http.get<OrdenSeguimientoDetallePreparacion[]>(`${this.apiUrl}/para-preparacion/${idOrden}`);
  }

  /* Seguimiento para entrega */
  getOrdenesParaEntrega(): Observable<OrdenSeguimiento[]> {
    return this.http.get<OrdenSeguimiento[]>(`${this.apiUrl}/para-entrega`);
  }

  getOrdenDetalleParaEntrega(idOrden: number): Observable<OrdenSeguimientoDetalleEntrega[]> {
    return this.http.get<OrdenSeguimientoDetalleEntrega[]>(`${this.apiUrl}/para-entrega/${idOrden}`);
  }

  /* Seguimiento para repartir */
  getOrdenesParaRepartir(): Observable<OrdenSeguimiento[]> {
    return this.http.get<OrdenSeguimiento[]>(`${this.apiUrl}/para-repartir`);
  }

  getOrdenDetalleParaRepartir(idOrden: number): Observable<OrdenSeguimientoDetalle[]> {
    return this.http.get<OrdenSeguimientoDetalle[]>(`${this.apiUrl}/para-repartir/${idOrden}`);
  }

  /* Seguimiento para facturacion */
  getOrdenesParaFacturacion(): Observable<OrdenFacturacion[]> {
    return this.http.get<OrdenFacturacion[]>(`${this.apiUrl}/para-facturacion`);
  }

  getOrdenDetalleParaFacturacion(idOrden: number): Observable<OrdenFacturacionDetalle[]> {
    return this.http.get<OrdenFacturacionDetalle[]>(`${this.apiUrl}/para-facturacion/${idOrden}`);
  }

  getByDetalle(idOrden: number, idOrdenDetalle: number): Observable<OrdenSeguimientoDetalle[]> {
    return this.http.get<OrdenSeguimientoDetalle[]>(`${this.apiUrl}/por-detalle/${idOrden}/${idOrdenDetalle}`);
  }

  getFullByOrden(idOrden: number): Observable<OrdenSeguimientoDetalle[]> {
    return this.http.get<OrdenSeguimientoDetalle[]>(`${this.apiUrl}/orden/${idOrden}`);
  }

  getPossibleStates(tipo: string, subTipo: string): Observable<ProductoTipoEstado[]> {
    return this.http.get<ProductoTipoEstado[]>(`${this.apiUrl}/posibles/${tipo}/${subTipo}`);
  }

  /* Obtiene lista de posibles estados por detalle */
  getEstadosPorDetalle(idOrden: number): Observable<EstadosPorDetalleDTO[]> {
    return this.http.get<EstadosPorDetalleDTO[]>(`${this.apiUrl}/estados-por-detalle/${idOrden}`);
  }

  advance(idOrden: number, idOrdenDetalle: number): Observable<OrdenSeguimientoDetalle> {
    return this.http.post<OrdenSeguimientoDetalle>(`${this.apiUrl}/avanzar/${idOrden}/${idOrdenDetalle}`, {});
  }

  reverse(idOrden: number, idOrdenDetalle: number): Observable<OrdenSeguimientoDetalle> {
    return this.http.post<OrdenSeguimientoDetalle>(`${this.apiUrl}/regresar/${idOrden}/${idOrdenDetalle}`, {});
  }

  assignTrabajo(idOrden: number, idOrdenDetalle: number, trabajador: string): Observable<Usuario> {
    return this.http.post<Usuario>(`/api/ordenes-trabajo/asignar-trabajo/${idOrden}/${idOrdenDetalle}/${encodeURIComponent(trabajador)}`, {});
  }

  getReparador(idOrden: number, idOrdenDetalle: number): Observable<Usuario | null> {
    return this.http.get<Usuario | null>(`/api/ordenes-trabajo/obtener-reparador/${idOrden}/${idOrdenDetalle}`);
  }

  getNormal(idOrden: number, idOrdenDetalle: number): Observable<Usuario | null> {
    return this.http.get<Usuario | null>(`/api/ordenes-trabajo/obtener-normal/${idOrden}/${idOrdenDetalle}`);
  }

  progresoTrabajo(idOrden: number, idOrdenDetalle: number, cantidadTrabajada: number): Observable<any> {
    return this.http.post(`/api/ordenes-trabajo/progreso-trabajo/${idOrden}/${idOrdenDetalle}/${cantidadTrabajada}`, {});
  }

  getPegadores(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`/api/usuarios/pegadores`);
  }
}
