import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Factura } from './factura.model'; 

@Injectable({
  providedIn: 'root',
})
export class FacturaService {
  private apiUrl = '/api/facturas';

  constructor(private http: HttpClient) {}

  getAllFiltered(searchTerm: string = '', estadoFilter: string = ''): Observable<Factura[]> {
    let params = new HttpParams();

    // Filtro de estado
    if (estadoFilter) {
      params = params.set('estado', estadoFilter);
    }

    // Filtro inteligente: si es solo números → busca por ID exacto, sino por cliente (LIKE)
    if (searchTerm?.trim()) {
      const trimmed = searchTerm.trim();
      if (/^\d+$/.test(trimmed)) {
        params = params.set('id', trimmed);
      } else {
        params = params.set('cliente', trimmed);
      }
    }

    return this.http.get<Factura[]>(`${this.apiUrl}`, { params });
  }

}
