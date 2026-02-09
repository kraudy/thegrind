import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenListComponent } from './orden-list.component';

describe('OrdenListComponent', () => {
  let component: OrdenListComponent;
  let fixture: ComponentFixture<OrdenListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
