import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Usuario } from './usuario.model';
import { UsuarioTrabajo } from './usuario-trabajo.model';

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  private apiUrl = '/api/usuarios';

  constructor(private http: HttpClient) {}

  getReparadores(): Observable<UsuarioTrabajo[]> {
    return this.http.get<UsuarioTrabajo[]>(`${this.apiUrl}/repara`);
  }

  getNormales(): Observable<UsuarioTrabajo[]> {
    return this.http.get<UsuarioTrabajo[]>(`${this.apiUrl}/normal`);
  }
}
