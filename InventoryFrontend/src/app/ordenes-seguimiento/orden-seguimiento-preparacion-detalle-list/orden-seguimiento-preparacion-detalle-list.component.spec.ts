import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenSeguimientoPreparacionDetalleListComponent } from './orden-seguimiento-preparacion-detalle-list.component';

describe('OrdenSeguimientoPreparacionDetalleListComponent', () => {
  let component: OrdenSeguimientoPreparacionDetalleListComponent;
  let fixture: ComponentFixture<OrdenSeguimientoPreparacionDetalleListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenSeguimientoPreparacionDetalleListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenSeguimientoPreparacionDetalleListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
