import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenSeguimientoRepartirListComponent } from './orden-seguimiento-repartir-list.component';

describe('OrdenSeguimientoRepartirListComponent', () => {
  let component: OrdenSeguimientoRepartirListComponent;
  let fixture: ComponentFixture<OrdenSeguimientoRepartirListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenSeguimientoRepartirListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenSeguimientoRepartirListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
