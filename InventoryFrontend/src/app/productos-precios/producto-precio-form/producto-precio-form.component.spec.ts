import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductoPrecioFormComponent } from './producto-precio-form.component';

describe('ProductoPrecioForm', () => {
  let component: ProductoPrecioFormComponent;
  let fixture: ComponentFixture<ProductoPrecioFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductoPrecioFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductoPrecioFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
