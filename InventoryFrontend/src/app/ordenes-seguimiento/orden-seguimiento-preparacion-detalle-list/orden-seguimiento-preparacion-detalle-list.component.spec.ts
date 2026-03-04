import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenSeguimientoPreparacionDetalleList } from './orden-seguimiento-preparacion-detalle-list.component';

describe('OrdenSeguimientoPreparacionDetalleList', () => {
  let component: OrdenSeguimientoPreparacionDetalleList;
  let fixture: ComponentFixture<OrdenSeguimientoPreparacionDetalleList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenSeguimientoPreparacionDetalleList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenSeguimientoPreparacionDetalleList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
