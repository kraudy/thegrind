import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenCalendarioFormComponent } from './orden-calendario-form.component';

describe('OrdenCalendarioFormComponent', () => {
  let component: OrdenCalendarioFormComponent;
  let fixture: ComponentFixture<OrdenCalendarioFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenCalendarioFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenCalendarioFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
