import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { OrdenCalendario } from './orden-calendario.model';
import { CalendarioDiaDTO } from './calendario-dia.model';

@Injectable({
  providedIn: 'root',
})
export class OrdenCalendarioService {
  private apiUrl = '/api/ordenes-calendario';

  constructor(
    private http: HttpClient
  ) {}

  getAll(): Observable<OrdenCalendario[]> {
    return this.http.get<OrdenCalendario[]>(this.apiUrl);
  }

  getById(idOrden: number): Observable<OrdenCalendario> {
    return this.http.get<OrdenCalendario>(`${this.apiUrl}/${idOrden}`);
  }

  getCalendario(): Observable<CalendarioDiaDTO[]> {
    return this.http.get<CalendarioDiaDTO[]>(`${this.apiUrl}/calendario`);
  }

  create(ordenDetalle: Partial<OrdenCalendario>): Observable<OrdenCalendario> {
    return this.http.post<OrdenCalendario>(`${this.apiUrl}`, ordenDetalle);
  }

  // Actualizar un calendario
  update(idOrden: number, ordenDetalle: Partial<OrdenCalendario>): Observable<OrdenCalendario> {
    return this.http.put<OrdenCalendario>(`${this.apiUrl}/${idOrden}`, ordenDetalle);
  }

  // Eliminar un calendario
  delete(idOrden: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${idOrden}`);
  }

  getByDate(fecha: string): Observable<OrdenCalendario[]> {
    return this.http.get<OrdenCalendario[]>(`${this.apiUrl}/por-fecha/${fecha}`);
  }
}
