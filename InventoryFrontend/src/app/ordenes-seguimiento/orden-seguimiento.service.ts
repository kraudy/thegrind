import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { OrdenSeguimientoDetalle } from './orden-seguimiento-detalle.model';
import { OrdenSeguimientoDetalleImpresion } from './orden-seguimiento-detalle-impresion.model';
import { ProductoTipoEstado } from '../productos-tipo-estados/producto-tipo-estado.model';
import { OrdenSeguimiento } from './orden-seguimiento.model';
import { EstadosPorDetalleDTO } from './estados-por-detalle.model';
import { Usuario } from '../usuarios/usuario.model';


@Injectable({
  providedIn: 'root',
})
export class OrdenSeguimientoService {
  private apiUrl = '/api/ordenes-seguimiento';

  constructor(private http: HttpClient) {}

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

  getOrdenDetalleParaPreparacion(idOrden: number): Observable<OrdenSeguimientoDetalle[]> {
    return this.http.get<OrdenSeguimientoDetalle[]>(`${this.apiUrl}/para-preparacion/${idOrden}`);
  }

  /* Seguimiento para entrega */
  getOrdenesParaEntrega(): Observable<OrdenSeguimiento[]> {
    return this.http.get<OrdenSeguimiento[]>(`${this.apiUrl}/para-entrega`);
  }

  getOrdenDetalleParaEntrega(idOrden: number): Observable<OrdenSeguimientoDetalle[]> {
    return this.http.get<OrdenSeguimientoDetalle[]>(`${this.apiUrl}/para-entrega/${idOrden}`);
  }

  /* Seguimiento para repartir */
  getOrdenesParaRepartir(): Observable<OrdenSeguimiento[]> {
    return this.http.get<OrdenSeguimiento[]>(`${this.apiUrl}/para-repartir`);
  }

  getOrdenDetalleParaRepartir(idOrden: number): Observable<OrdenSeguimientoDetalle[]> {
    return this.http.get<OrdenSeguimientoDetalle[]>(`${this.apiUrl}/para-repartir/${idOrden}`);
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
