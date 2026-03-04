import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenSeguimientoPreparacionListComponent } from './orden-seguimiento-preparacion-list.component';

describe('OrdenSeguimientoPreparacionListComponent', () => {
  let component: OrdenSeguimientoPreparacionListComponent;
  let fixture: ComponentFixture<OrdenSeguimientoPreparacionListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenSeguimientoPreparacionListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenSeguimientoPreparacionListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
