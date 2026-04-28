import { TestBed } from '@angular/core/testing';

import { OrdenCostoService } from './orden-costo.service';

describe('OrdenCostoService', () => {
  let service: OrdenCostoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OrdenCostoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
