import { TestBed } from '@angular/core/testing';

import { ProductoSubTipoService } from './producto-sub-tipo.service';

describe('ProductoSubTipoService', () => {
  let service: ProductoSubTipoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProductoSubTipoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
