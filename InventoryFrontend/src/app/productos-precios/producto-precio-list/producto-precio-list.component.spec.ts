import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductoPrecioListComponent } from './producto-precio-list.component';

describe('ProductoPrecioListComponent', () => {
  let component: ProductoPrecioListComponent;
  let fixture: ComponentFixture<ProductoPrecioListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductoPrecioListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductoPrecioListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
