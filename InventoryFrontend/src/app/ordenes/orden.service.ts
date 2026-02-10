import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Orden } from './orden.model';

@Injectable({
  providedIn: 'root',
})
export class OrdenService {
  private apiUrl = '/api/ordenes';

  constructor(
    private http: HttpClient
  ) {}

  getAll(): Observable<Orden[]> {
    return this.http.get<Orden[]>(this.apiUrl);
  }

  getById(id: number): Observable<Orden> {
    return this.http.get<Orden>(`${this.apiUrl}/${id}`);
  }

  create(orden: Partial<Orden>): Observable<Orden> {
    return this.http.post<Orden>(this.apiUrl, orden);
  }

  update(id: number, orden: Partial<Orden>): Observable<Orden> {
    return this.http.put<Orden>(`${this.apiUrl}/${id}`, orden);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}