import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ProductoTipo } from './producto-tipo.model';

@Injectable({
  providedIn: 'root',
})
export class ProductoTipoService {
  private apiUrl = '/api/productos-tipos'; // Proxy route to backend

  constructor(
    private http: HttpClient
  ) {}

  getAll(): Observable<ProductoTipo[]> {
    return this.http.get<ProductoTipo[]>(this.apiUrl);
  }

  getById(tipo: string): Observable<ProductoTipo> {
    return this.http.get<ProductoTipo>(`${this.apiUrl}/${tipo}`);
  }

  create(producto: Partial<ProductoTipo>): Observable<ProductoTipo> {
    return this.http.post<ProductoTipo>(this.apiUrl, producto);
  }

  update(tipo: string, producto: Partial<ProductoTipo>): Observable<ProductoTipo> {
    return this.http.put<ProductoTipo>(`${this.apiUrl}/${tipo}`, producto);
  }

  delete(tipo: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${tipo}`);
  }
}
