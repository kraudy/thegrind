import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FacturaDetalle } from './factura-detalle.model'; 

@Injectable({
  providedIn: 'root',
})
export class FacturaDetalleService {
  private apiUrl = '/api/facturas-detalle';

  constructor(private http: HttpClient) {}

  getById(id: number, detalle: number): Observable<FacturaDetalle[]> {
    return this.http.get<FacturaDetalle[]>(`${this.apiUrl}/${id}/${detalle}`);
  }

  crearDesdeOrden(idOrden: number): Observable<FacturaDetalle> {
    return this.http.post<FacturaDetalle>(`${this.apiUrl}/${idOrden}`, null, { responseType: 'json' });
  }

}
