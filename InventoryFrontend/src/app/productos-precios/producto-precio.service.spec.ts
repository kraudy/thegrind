import { TestBed } from '@angular/core/testing';

import { ProductoPrecioService } from './producto-precio.service';

describe('ProductoPrecioService', () => {
  let service: ProductoPrecioService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProductoPrecioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
