import { Component, Input, Output, EventEmitter, OnChanges, } from '@angular/core';

import { CommonModule, Location  } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { ProductService } from '../product.service';
import { Product } from '../product.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-form.html',
  styleUrls: ['./product-form.css'],
})
export class ProductFormComponent implements OnChanges{
  @Input() product: Product | null = null;
  @Output() productSaved = new EventEmitter<void>();
  isEdit = false;
  productId: number | null = null;

  constructor(
    private productService: ProductService,
    private location: Location
  ) {}

  formProduct: Product = { name: '', description: '', quantity: 0};

  ngOnChanges(): void {
    if (this.product) {
      this.formProduct = { ...this.product };
      this.isEdit = !!this.product.id;  // true if editing an existing product
    } else {
      this.resetForm();
      this.isEdit = false;
    }
  }

  goBack(): void {
    this.location.back();
  }

  resetForm(): void {
    this.formProduct = { name: '', description: '', quantity: 0};
  }

  onSubmit(): void {
    if (this.formProduct.id) {
      console.log('Updating product: ' + this.formProduct.id);
      this.productService.update(this.formProduct.id, this.formProduct).subscribe(() => {
        this.productSaved.emit();
        this.resetForm();
      });
    } else {
      console.log('Creating product: ');
      this.productService.create(this.formProduct).subscribe(() => {
        this.productSaved.emit();
        this.resetForm();
      });
    }
  }

}
