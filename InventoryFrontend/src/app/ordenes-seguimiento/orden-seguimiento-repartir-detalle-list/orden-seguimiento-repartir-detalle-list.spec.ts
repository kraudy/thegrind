import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenSeguimientoRepartirDetalleList } from './orden-seguimiento-repartir-detalle-list';

describe('OrdenSeguimientoRepartirDetalleList', () => {
  let component: OrdenSeguimientoRepartirDetalleList;
  let fixture: ComponentFixture<OrdenSeguimientoRepartirDetalleList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenSeguimientoRepartirDetalleList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenSeguimientoRepartirDetalleList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
