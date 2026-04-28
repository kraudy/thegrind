import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenCostoFormComponent } from './orden-costo-form.component';

describe('OrdenCostoFormComponent', () => {
  let component: OrdenCostoFormComponent;
  let fixture: ComponentFixture<OrdenCostoFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenCostoFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenCostoFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
