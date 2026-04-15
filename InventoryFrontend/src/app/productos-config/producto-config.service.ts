import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductoConfig } from './producto-config.model';

@Injectable({
  providedIn: 'root'
})
export class ProductoConfigService {
  private apiUrl = '/api/productos/config';

  constructor(private http: HttpClient) {}

  getConfig(
    tipo?: string,
    subTipo?: string,
    medida?: string,
    modelo?: string,
    color?: string
  ): Observable<ProductoConfig[]> {

    let params = new HttpParams();

    if (tipo && tipo !== 'Ninguno') params = params.set('tipo', tipo);
    if (subTipo && subTipo !== 'Ninguno') params = params.set('subTipo', subTipo);
    if (medida && medida !== 'Ninguno') params = params.set('medida', medida);
    if (modelo && modelo !== 'Ninguno') params = params.set('modelo', modelo);
    if (color && color !== 'Ninguno') params = params.set('color', color);

    return this.http.get<ProductoConfig[]>(this.apiUrl, { params });
  }
}