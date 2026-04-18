import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { UsuarioAdminDTO } from './usuario-admin.model';
import { CreateUsuarioRequest } from './usuario-create.model';
import { UsuarioNombre } from './usuario-nombre.model';
import { UsuarioTrabajo } from './usuario-trabajo.model';

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  private apiUrl = '/api/usuarios';

  constructor(private http: HttpClient) {}

  getAll(): Observable<UsuarioAdminDTO[]> {
    return this.http.get<UsuarioAdminDTO[]>(this.apiUrl);
  }

  getById(usuario: string): Observable<UsuarioAdminDTO> {
    return this.http.get<UsuarioAdminDTO>(`${this.apiUrl}/${usuario}`);
  }

  getAllRoles(): Observable<string[]> {
    return this.http.get<string[]>('/api/roles');
  }

  create(req: CreateUsuarioRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}`, req, { responseType: 'text' });
  }

  toggleActivo(usuario: string, activo: boolean): Observable<UsuarioAdminDTO> {
    return this.http.put<UsuarioAdminDTO>(`${this.apiUrl}/${usuario}/activo`, activo);
  }

  resetPassword(usuario: string, password: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/${usuario}/reset-password`, { password }, { responseType: 'text' });
  }

  assignRole(usuario: string, rol: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/${usuario}/roles`, { rol }, { responseType: 'text' });
  }

  removeRole(usuario: string, rol: string): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${usuario}/roles/${rol}`, { responseType: 'text' });
  }

  getReparadores(): Observable<UsuarioTrabajo[]> {
    return this.http.get<UsuarioTrabajo[]>(`${this.apiUrl}/repara`);
  }

  getNormales(): Observable<UsuarioTrabajo[]> {
    return this.http.get<UsuarioTrabajo[]>(`${this.apiUrl}/normal`);
  }
}
