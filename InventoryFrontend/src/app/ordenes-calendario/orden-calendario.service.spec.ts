import { TestBed } from '@angular/core/testing';

import { OrdenCalendarioService } from './orden-calendario.service';

describe('OrdenCalendarioService', () => {
  let service: OrdenCalendarioService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OrdenCalendarioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
