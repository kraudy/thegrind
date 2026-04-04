import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Orden } from './orden.model';

@Injectable({
  providedIn: 'root',
})
export class OrdenService {
  private apiUrl = '/api/ordenes';

  constructor(
    private http: HttpClient
  ) {}

  getAll(): Observable<Orden[]> {
    return this.http.get<Orden[]>(this.apiUrl);
  }

  getAllFiltered(searchTerm: string = '', estadoFilter: string = ''): Observable<Orden[]> {
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

    return this.http.get<Orden[]>(this.apiUrl, { params });
  }

  getRecibidas(): Observable<Orden[]> {
    return this.http.get<Orden[]>(`${this.apiUrl}/recibidas`);
  }

  getById(id: number): Observable<Orden> {
    return this.http.get<Orden>(`${this.apiUrl}/${id}`);
  }

  create(orden: Partial<Orden>): Observable<Orden> {
    return this.http.post<Orden>(this.apiUrl, orden);
  }

  update(id: number, orden: Partial<Orden>): Observable<Orden> {
    return this.http.put<Orden>(`${this.apiUrl}/${id}`, orden);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}