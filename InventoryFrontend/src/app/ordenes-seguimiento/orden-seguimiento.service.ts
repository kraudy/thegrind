import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { OrdenSeguimientoDetalle } from './orden-seguimiento-detalle.model';
import { ProductoTipoEstado } from '../productos-tipo-estados/producto-tipo-estado.model';
import { OrdenSeguimiento } from './orden-seguimiento.model';
import { EstadosPorDetalleDTO } from './estados-por-detalle.model';


@Injectable({
  providedIn: 'root',
})
export class OrdenSeguimientoService {
  private apiUrl = '/api/ordenes-seguimiento';

  constructor(private http: HttpClient) {}

  getOrdenesParaImpresion(): Observable<OrdenSeguimiento[]> {
    return this.http.get<OrdenSeguimiento[]>(`${this.apiUrl}/para-impresion`);
  }

  getOrdenDetalleParaImpresion(idOrden: number): Observable<OrdenSeguimientoDetalle[]> {
    return this.http.get<OrdenSeguimientoDetalle[]>(`${this.apiUrl}/para-impresion/${idOrden}`);
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
}
