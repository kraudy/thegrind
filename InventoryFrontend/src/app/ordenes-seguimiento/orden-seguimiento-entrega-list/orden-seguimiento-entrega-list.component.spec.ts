import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenSeguimientoEntregaListComponent } from './orden-seguimiento-entrega-list.component';

describe('OrdenSeguimientoEntregaListComponent', () => {
  let component: OrdenSeguimientoEntregaListComponent;
  let fixture: ComponentFixture<OrdenSeguimientoEntregaListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenSeguimientoEntregaListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenSeguimientoEntregaListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
