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

  getByDetalle(idOrden: number, idOrdenDetalle: number, idProducto: number): Observable<OrdenSeguimiento[]> {
    return this.http.get<OrdenSeguimiento[]>(`${this.apiUrl}/por-detalle/${idOrden}/${idOrdenDetalle}/${idProducto}`);
  }

  getFullByOrden(idOrden: number): Observable<OrdenSeguimiento[]> {
    return this.http.get<OrdenSeguimiento[]>(`${this.apiUrl}/orden/${idOrden}`);
  }

  //TODO: Move this to its own service
  getPossibleStates(tipo: string, subTipo: string): Observable<ProductoTipoEstado[]> {
    return this.http.get<ProductoTipoEstado[]>(`${this.apiUrl}/posibles/${tipo}/${subTipo}`);
  }

  advance(idOrden: number, idOrdenDetalle: number, idProducto: number): Observable<OrdenSeguimiento> {
    return this.http.post<OrdenSeguimiento>(`${this.apiUrl}/avanzar/${idOrden}/${idOrdenDetalle}/${idProducto}`, {});
  }
}
