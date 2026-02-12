import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductoTipoFormComponent } from './producto-tipo-form.component';

describe('ProductoTipoForm', () => {
  let component: ProductoTipoFormComponent;
  let fixture: ComponentFixture<ProductoTipoFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductoTipoFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductoTipoFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
