import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrdenPago } from './orden-pago.model'; 

@Injectable({
  providedIn: 'root',
})
export class OrdenPagoService {
  private apiUrl = '/api/ordenes-pago';

  constructor(private http: HttpClient) {}

  getPagosByOrden(idOrden: number): Observable<OrdenPago[]> {
    return this.http.get<OrdenPago[]>(`${this.apiUrl}/por-orden/${idOrden}`);
  }

  registrarPago(idOrden: number, pago: Partial<OrdenPago>): Observable<OrdenPago> {
    return this.http.post<OrdenPago>(`${this.apiUrl}/${idOrden}`, pago);
  }

  getPagosPendientes(search: string = '', estado: string = ''): Observable<OrdenPago[]> {
    let params: any = {};
    if (search?.trim()) params.search = search.trim();
    if (estado) params.estado = estado;
    return this.http.get<OrdenPago[]>(`${this.apiUrl}/pagos`, { params });
  }

  aprobarPago(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/aprobar`, {});
  }

  rechazarPago(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/rechazar`, {});
  }
}
