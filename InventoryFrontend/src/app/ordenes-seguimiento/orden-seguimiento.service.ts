import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { OrdenSeguimiento } from './orden-seguimiento.model';
import { ProductoTipoEstado } from '../productos-tipo-estados/producto-tipo-estado.model';


@Injectable({
  providedIn: 'root',
})
export class OrdenSeguimientoService {
  private apiUrl = '/api/ordenes-seguimiento';

  constructor(private http: HttpClient) {}

  getByDetalle(idOrden: number, idOrdenDetalle: number): Observable<OrdenSeguimiento[]> {
    return this.http.get<OrdenSeguimiento[]>(`${this.apiUrl}/por-detalle/${idOrden}/${idOrdenDetalle}`);
  }

  getFullByOrden(idOrden: number): Observable<OrdenSeguimiento[]> {
    return this.http.get<OrdenSeguimiento[]>(`${this.apiUrl}/orden/${idOrden}`);
  }

  //TODO: Move this to its own service
  // maybe just generate a map with a list of strings to get all at the same time on load in the component 
  // end then just key them in the ngFor
  getPossibleStates(tipo: string, subTipo: string): Observable<ProductoTipoEstado[]> {
    return this.http.get<ProductoTipoEstado[]>(`${this.apiUrl}/posibles/${tipo}/${subTipo}`);
  }

  advance(idOrden: number, idOrdenDetalle: number): Observable<OrdenSeguimiento> {
    return this.http.post<OrdenSeguimiento>(`${this.apiUrl}/avanzar/${idOrden}/${idOrdenDetalle}`, {});
  }
}
