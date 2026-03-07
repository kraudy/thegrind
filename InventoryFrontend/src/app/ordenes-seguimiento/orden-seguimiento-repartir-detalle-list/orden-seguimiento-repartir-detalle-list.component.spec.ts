import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenSeguimientoRepartirDetalleListComponent } from './orden-seguimiento-repartir-detalle-list.component';

describe('OrdenSeguimientoRepartirDetalleListComponent', () => {
  let component: OrdenSeguimientoRepartirDetalleListComponent;
  let fixture: ComponentFixture<OrdenSeguimientoRepartirDetalleListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenSeguimientoRepartirDetalleListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenSeguimientoRepartirDetalleListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
