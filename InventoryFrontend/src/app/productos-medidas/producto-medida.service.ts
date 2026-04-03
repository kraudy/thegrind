import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductoMedida } from './producto-medida.model';

@Injectable({
  providedIn: 'root',
})
export class ProductoMedidaService {
  private apiUrl = '/api/productos-medidas';
  
  constructor(
    private http: HttpClient
  ) {}

  getAll(): Observable<ProductoMedida[]> {
    return this.http.get<ProductoMedida[]>(this.apiUrl);
  }

  getById(medida: string): Observable<ProductoMedida> {
    return this.http.get<ProductoMedida>(`${this.apiUrl}/${medida}`);
  }

  create(productoMedida: Partial<ProductoMedida>): Observable<ProductoMedida> {
    return this.http.post<ProductoMedida>(this.apiUrl, productoMedida);
  }
  
}
