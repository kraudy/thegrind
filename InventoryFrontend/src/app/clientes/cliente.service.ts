import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Cliente } from './cliente.model';

@Injectable({
  providedIn: 'root',
})
export class ClienteService {
  private apiUrl = '/api/clientes';

  constructor(
    private http: HttpClient
  ) {}

  getAll(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(this.apiUrl);
  }

  getAllWithFilters(filters: {
    id?: number;
    nombre?: string;
    telefono?: string;
    correo?: string;
  } = {}): Observable<Cliente[]> {
    let params = new HttpParams();
    if (filters.id !== undefined) {
      params = params.set('id', filters.id.toString());
    }
    if (filters.nombre) {
      params = params.set('nombre', filters.nombre);
    }
    if (filters.telefono) {
      params = params.set('telefono', filters.telefono);
    }
    if (filters.correo) {
      params = params.set('correo', filters.correo);
    }
    return this.http.get<Cliente[]>(this.apiUrl, { params });
  }

  getById(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.apiUrl}/${id}`);
  }

  create(cliente: Partial<Cliente>): Observable<Cliente> {
    return this.http.post<Cliente>(this.apiUrl, cliente);
  }

  update(id: number, cliente: Partial<Cliente>): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.apiUrl}/${id}`, cliente);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  search(termino: string): Observable<Cliente[]> {
    const params = new HttpParams().set('termino', termino.trim());
    return this.http.get<Cliente[]>(`${this.apiUrl}/buscar`, { params });
  }
}
