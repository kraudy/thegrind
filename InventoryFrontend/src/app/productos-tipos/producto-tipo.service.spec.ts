import { TestBed } from '@angular/core/testing';

import { ProductoTipoService } from './producto-tipo.service';

describe('ProductoTipoService', () => {
  let service: ProductoTipoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProductoTipoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
