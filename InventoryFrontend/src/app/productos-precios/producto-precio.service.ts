import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductoPrecio } from './producto-precio.model';

@Injectable({
  providedIn: 'root',
})
export class ProductoPrecioService {
  private apiUrl = '/api/productos-precios';
  
  constructor(
    private http: HttpClient
  ) {}

  getAll(): Observable<ProductoPrecio[]> {
    return this.http.get<ProductoPrecio[]>(this.apiUrl);
  }

  getByComposite(productoId: number, precio: number): Observable<ProductoPrecio> {
    return this.http.get<ProductoPrecio>(`${this.apiUrl}/${productoId}/${precio}`);
  }

  create(productoPrecio: Partial<ProductoPrecio>): Observable<ProductoPrecio> {
    return this.http.post<ProductoPrecio>(this.apiUrl, productoPrecio);
  }

  updateComposite(productoId: number, precio: number, productoPrecio: Partial<ProductoPrecio>): Observable<ProductoPrecio> {
    return this.http.put<ProductoPrecio>(`${this.apiUrl}/${productoId}/${precio}`, productoPrecio);
  }

  deleteComposite(productoId: number, precio: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${productoId}/${precio}`);
  }
  
}
