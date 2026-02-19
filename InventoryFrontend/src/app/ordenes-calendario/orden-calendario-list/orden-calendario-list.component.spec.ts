import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenCalendarioListComponent } from './orden-calendario-list.component';

describe('OrdenCalendarioListComponent', () => {
  let component: OrdenCalendarioListComponent;
  let fixture: ComponentFixture<OrdenCalendarioListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenCalendarioListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenCalendarioListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
