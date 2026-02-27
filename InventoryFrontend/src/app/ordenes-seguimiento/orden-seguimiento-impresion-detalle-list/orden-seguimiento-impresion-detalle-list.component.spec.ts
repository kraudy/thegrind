import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenSeguimientoImpresionDetalleListComponent } from './orden-seguimiento-impresion-detalle-list.component';

describe('OrdenSeguimientoImpresionDetalleListComponent', () => {
  let component: OrdenSeguimientoImpresionDetalleListComponent;
  let fixture: ComponentFixture<OrdenSeguimientoImpresionDetalleListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenSeguimientoImpresionDetalleListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenSeguimientoImpresionDetalleListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
