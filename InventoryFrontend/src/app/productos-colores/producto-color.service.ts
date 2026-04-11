import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductoColor } from './producto-color.model';

@Injectable({
  providedIn: 'root',
})
export class ProductoColorService {
  private apiUrl = '/api/productos-colores';

  constructor(private http: HttpClient) {}

  getAll(): Observable<ProductoColor[]> {
    return this.http.get<ProductoColor[]>(this.apiUrl);
  }

  getById(color: string): Observable<ProductoColor> {
    return this.http.get<ProductoColor>(`${this.apiUrl}/${color}`);
  }

  create(productoColor: Partial<ProductoColor>): Observable<ProductoColor> {
    return this.http.post<ProductoColor>(this.apiUrl, productoColor);
  }

  update(color: string, productoColor: Partial<ProductoColor>): Observable<ProductoColor> {
    return this.http.put<ProductoColor>(`${this.apiUrl}/${color}`, productoColor);
  }

  delete(color: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${color}`);
  }
}