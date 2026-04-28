import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { OrdenCosto } from './orden-costo.model';

@Injectable({
  providedIn: 'root',
})
export class OrdenCostoService {
  private apiUrl = '/api/ordenes-costo';
  
  constructor(
    private http: HttpClient
  ) {}

  getAll(
    tipoCosto: string,
    trabajador: string,
    filters: {
    fechaInicio?: string;
    fechaFin?: string;
    idOrden?: number;
    idOrdenDetalle?: number;
    pagado?: boolean;
  } = {}): Observable<OrdenCosto[]> {
    let params = new HttpParams();

    if (filters.fechaInicio)                  params = params.set('fechaInicio', filters.fechaInicio);
    if (filters.fechaFin)                     params = params.set('fechaFin', filters.fechaFin);
    if (filters.idOrden !== undefined)        params = params.set('idOrden', filters.idOrden.toString());
    if (filters.idOrdenDetalle !== undefined) params = params.set('idOrdenDetalle', filters.idOrdenDetalle.toString());
    if (filters.pagado !== undefined)         params = params.set('pagado', filters.pagado.toString());

    return this.http.get<OrdenCosto[]>(
      `${this.apiUrl}/${encodeURIComponent(tipoCosto)}/${encodeURIComponent(trabajador)}`,
      { params }
    );
  }

  getTotal(
    tipoCosto: string,
    trabajador: string,
    filters: {
      fechaInicio?: string;
      fechaFin?: string;
      idOrden?: number;
      idOrdenDetalle?: number;
      pagado?: boolean;
    } = {}): Observable<number> {
      let params = new HttpParams();

      if (filters.fechaInicio)                  params = params.set('fechaInicio', filters.fechaInicio);
      if (filters.fechaFin)                     params = params.set('fechaFin', filters.fechaFin);
      if (filters.idOrden !== undefined)        params = params.set('idOrden', filters.idOrden.toString());
      if (filters.idOrdenDetalle !== undefined) params = params.set('idOrdenDetalle', filters.idOrdenDetalle.toString());
      if (filters.pagado !== undefined)         params = params.set('pagado', filters.pagado.toString());

      return this.http.get<number>(
        `${this.apiUrl}/total/${encodeURIComponent(tipoCosto)}/${encodeURIComponent(trabajador)}`,
        { params }
      );
    }

  pagar(
    tipoCosto: string,
    trabajador: string,
    filters: {
    fechaInicio?: string;
    fechaFin?: string;
    idOrden?: number;
    idOrdenDetalle?: number;
  } = {}): Observable<void> {
    let params = new HttpParams();

    if (filters.fechaInicio) params = params.set('fechaInicio', filters.fechaInicio);
    if (filters.fechaFin) params = params.set('fechaFin', filters.fechaFin);
    if (filters.idOrden !== undefined) params = params.set('idOrden', filters.idOrden.toString());
    if (filters.idOrdenDetalle !== undefined) params = params.set('idOrdenDetalle', filters.idOrdenDetalle.toString());

    return this.http.post<void>(
      `${this.apiUrl}/pagar/${encodeURIComponent(tipoCosto)}/${encodeURIComponent(trabajador)}`,
      {},
      { params }
    );
  }

}
