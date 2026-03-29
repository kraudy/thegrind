import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrdenDetalle } from './orden-detalle.model';

@Injectable({
  providedIn: 'root',
})
export class OrdenDetalleService {
  private apiUrl = '/api/ordenes-detalle';

  constructor(
    private http: HttpClient
  ) {}

  getAll(): Observable<OrdenDetalle[]> {
    return this.http.get<OrdenDetalle[]>(this.apiUrl);
  }

  getByOrden(idOrden: number): Observable<OrdenDetalle[]> {
    return this.http.get<OrdenDetalle[]>(`${this.apiUrl}/por-orden/${idOrden}`);
  }

  getPdf(idOrden: number): Observable<Blob> {
  return this.http.get(`${this.apiUrl}/${idOrden}/pdf`, { 
    responseType: 'blob' 
  });
}

  // Obtener un detalle específico por clave compuesta
  getById(idOrden: number, idOrdenDetalle: number): Observable<OrdenDetalle> {
    return this.http.get<OrdenDetalle>(
      `${this.apiUrl}/${idOrden}/${idOrdenDetalle}`
    );
  }

  //create(ordenDetalle: Partial<OrdenDetalle>): Observable<OrdenDetalle> {
  create(idOrden: number, idProducto: number, ordenDetalle: Partial<OrdenDetalle>): Observable<OrdenDetalle> {
    return this.http.post<OrdenDetalle>(`${this.apiUrl}/${idOrden}/${idProducto}`, ordenDetalle);
  }

  // Actualizar un detalle existente (por clave compuesta)
  update(idOrden: number, idOrdenDetalle: number, ordenDetalle: Partial<OrdenDetalle>): Observable<OrdenDetalle> {
    return this.http.put<OrdenDetalle>(
      `${this.apiUrl}/${idOrden}/${idOrdenDetalle}`,
      ordenDetalle
    );
  }

  // Eliminar un detalle (por clave compuesta)
  delete(idOrden: number, idOrdenDetalle: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${idOrden}/${idOrdenDetalle}`
    );
  }
}
