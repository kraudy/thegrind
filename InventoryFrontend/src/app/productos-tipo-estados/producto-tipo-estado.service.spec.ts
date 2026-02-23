import { TestBed } from '@angular/core/testing';

import { ProductoTipoEstadoService } from './producto-tipo-estado.service';

describe('ProductoTipoEstadoService', () => {
  let service: ProductoTipoEstadoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProductoTipoEstadoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
