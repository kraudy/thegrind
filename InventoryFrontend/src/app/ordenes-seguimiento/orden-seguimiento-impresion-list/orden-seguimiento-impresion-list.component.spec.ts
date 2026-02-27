import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenSeguimientoImpresionListComponent } from './orden-seguimiento-impresion-list.component';

describe('OrdenSeguimientoImpresionListComponent', () => {
  let component: OrdenSeguimientoImpresionListComponent;
  let fixture: ComponentFixture<OrdenSeguimientoImpresionListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenSeguimientoImpresionListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenSeguimientoImpresionListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
