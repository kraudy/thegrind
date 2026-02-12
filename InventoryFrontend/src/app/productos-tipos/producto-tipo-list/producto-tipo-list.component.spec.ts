import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductoTipoListComponent } from './producto-tipo-list.component';

describe('ProductoTipoList', () => {
  let component: ProductoTipoListComponent;
  let fixture: ComponentFixture<ProductoTipoListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductoTipoListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductoTipoListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
