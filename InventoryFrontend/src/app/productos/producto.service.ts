import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Producto } from './producto.model';

@Injectable({
  providedIn: 'root'
})
export class ProductoService {
  private apiUrl = '/api/productos';  // Proxy route to backend

  constructor(
    private http: HttpClient
  ) {}

  getById(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/${id}`);
  }

  create(producto: Partial<Producto>): Observable<Producto> {
    return this.http.post<Producto>(this.apiUrl, producto);
  }

  update(id: number, producto: Partial<Producto>): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/${id}`, producto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  search(q: string): Observable<Producto[]> {
    const params = new HttpParams().set('q', q.trim());
    return this.http.get<Producto[]>(`${this.apiUrl}/search`, { params });
  }

  getAllWithFilters(filters: {
    id?: number;
    nombre?: string;
    tipo?: string;
    subTipo?: string;
    sinPrecio?: boolean;
  } = {}): Observable<Producto[]> {
    let params = new HttpParams();

    if (filters.id !== undefined) {
      params = params.set('id', filters.id.toString());
    }
    if (filters.nombre) {
      params = params.set('nombre', filters.nombre);
    }
    if (filters.tipo) {
      params = params.set('tipo', filters.tipo);
    }
    if (filters.subTipo) {
      params = params.set('subTipo', filters.subTipo);
    }
    if (filters.sinPrecio !== undefined) {
      params = params.set('sinPrecio', filters.sinPrecio.toString()); // true/false as string
    }

    return this.http.get<Producto[]>(this.apiUrl, { params });
  }

}
