import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenSeguimientoEntregaDetalleListComponent } from './orden-seguimiento-entrega-detalle-list.component';

describe('OrdenSeguimientoEntregaDetalleListComponent', () => {
  let component: OrdenSeguimientoEntregaDetalleListComponent;
  let fixture: ComponentFixture<OrdenSeguimientoEntregaDetalleListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenSeguimientoEntregaDetalleListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenSeguimientoEntregaDetalleListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
