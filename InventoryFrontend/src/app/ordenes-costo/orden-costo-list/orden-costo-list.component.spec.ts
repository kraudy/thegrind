import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenCostoListComponent } from './orden-costo-list.component';

describe('OrdenCostoListComponent', () => {
  let component: OrdenCostoListComponent;
  let fixture: ComponentFixture<OrdenCostoListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenCostoListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenCostoListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
