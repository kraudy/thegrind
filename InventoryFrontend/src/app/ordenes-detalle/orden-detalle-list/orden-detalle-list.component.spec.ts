import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenDetalleListComponent } from './orden-detalle-list.component';

describe('OrdenDetalleListComponent', () => {
  let component: OrdenDetalleListComponent;
  let fixture: ComponentFixture<OrdenDetalleListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenDetalleListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenDetalleListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
