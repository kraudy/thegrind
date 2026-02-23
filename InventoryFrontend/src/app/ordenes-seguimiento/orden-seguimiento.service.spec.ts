import { TestBed } from '@angular/core/testing';

import { OrdenSeguimientoService } from './orden-seguimiento.service';

describe('OrdenSeguimiento', () => {
  let service: OrdenSeguimientoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OrdenSeguimientoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
