import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductoCosto } from './producto-costo.model';

@Injectable({
  providedIn: 'root',
})
export class ProductoCostoService {
  private apiUrl = '/api/productos-costos';
  
  constructor(
    private http: HttpClient
  ) {}

  getAll(): Observable<ProductoCosto[]> {
    return this.http.get<ProductoCosto[]>(this.apiUrl);
  }

  getCostosByProducto(productoId: number): Observable<ProductoCosto[]> {
    return this.http.get<ProductoCosto[]>(`${this.apiUrl}/${productoId}`);
  }

  getByComposite(productoId: number, tipoCosto: string): Observable<ProductoCosto> {
    return this.http.get<ProductoCosto>(`${this.apiUrl}/${productoId}/${tipoCosto}`);
  }

  create(productoCosto: Partial<ProductoCosto>): Observable<ProductoCosto> {
    return this.http.post<ProductoCosto>(this.apiUrl, productoCosto);
  }

  updateComposite(productoId: number, tipoCosto: string, productoCosto: Partial<ProductoCosto>): Observable<ProductoCosto> {
    return this.http.put<ProductoCosto>(`${this.apiUrl}/${productoId}/${tipoCosto}`, productoCosto);
  }

  deleteComposite(productoId: number, tipoCosto: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${productoId}/${tipoCosto}`);
  }
  
}
