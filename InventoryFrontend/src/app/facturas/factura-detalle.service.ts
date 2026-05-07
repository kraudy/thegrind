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

  getByFactura(idFactura: number): Observable<FacturaDetalle[]> {
    return this.http.get<FacturaDetalle[]>(`${this.apiUrl}/${idFactura}`);
  }

  getById(idFactura: number, idDetalle: number): Observable<FacturaDetalle> {
    return this.http.get<FacturaDetalle>(`${this.apiUrl}/${idFactura}/${idDetalle}`);
  }

}
