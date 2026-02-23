import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenSeguimientoListComponent } from './orden-seguimiento-list.component';

describe('OrdenSeguimientoListComponent', () => {
  let component: OrdenSeguimientoListComponent;
  let fixture: ComponentFixture<OrdenSeguimientoListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenSeguimientoListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenSeguimientoListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
