import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenDetalleFormComponent } from './orden-detalle-form.component';

describe('OrdenDetalleFormComponent', () => {
  let component: OrdenDetalleFormComponent;
  let fixture: ComponentFixture<OrdenDetalleFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenDetalleFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenDetalleFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
