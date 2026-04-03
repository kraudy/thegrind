  import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductoModelo } from './producto-modelo.model';

@Injectable({
  providedIn: 'root',
})
export class ProductoModeloService {
  private apiUrl = '/api/productos-modelos';
  
  constructor(
    private http: HttpClient
  ) {}

  getAll(): Observable<ProductoModelo[]> {
    return this.http.get<ProductoModelo[]>(this.apiUrl);
  }

  getById(modelo: string): Observable<ProductoModelo> {
    return this.http.get<ProductoModelo>(`${this.apiUrl}/${modelo}`);
  }

  create(productoModelo: Partial<ProductoModelo>): Observable<ProductoModelo> {
    return this.http.post<ProductoModelo>(this.apiUrl, productoModelo);
  }
  
}
