import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ProductoSubTipo } from './producto-sub-tipo.model';

@Injectable({
  providedIn: 'root',
})
export class ProductoSubTipoService {
  private apiUrl = '/api/productos-sub-tipos'; // Proxy route to backend
  
    constructor(
      private http: HttpClient
    ) {}
  
    getAll(): Observable<ProductoSubTipo[]> {
      return this.http.get<ProductoSubTipo[]>(this.apiUrl);
    }
  
    getById(tipo: string): Observable<ProductoSubTipo> {
      return this.http.get<ProductoSubTipo>(`${this.apiUrl}/${tipo}`);
    }
  
    create(producto: Partial<ProductoSubTipo>): Observable<ProductoSubTipo> {
      return this.http.post<ProductoSubTipo>(this.apiUrl, producto);
    }
  
    update(tipo: string, producto: Partial<ProductoSubTipo>): Observable<ProductoSubTipo> {
      return this.http.put<ProductoSubTipo>(`${this.apiUrl}/${tipo}`, producto);
    }
  
    delete(tipo: string): Observable<void> {
      return this.http.delete<void>(`${this.apiUrl}/${tipo}`);
    }
}
