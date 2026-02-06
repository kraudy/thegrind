import { Component, Input, Output, EventEmitter, OnChanges } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { ProductService } from '../product.service';
import { Product } from '../product.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-form.html',
  styleUrl: './product-form.css',
})
export class ProductFormComponent implements OnChanges{
  @Input() product: Product | null = null;
  @Output() productSaved = new EventEmitter<void>();

  constructor(private productService: ProductService) {}

  formProduct: Product = { name: '', description: '', quantity: 0, price: 0 };

  ngOnChanges(): void {
    if (this.product) {
      this.formProduct = { ...this.product };
    } else {
      this.resetForm();
    }
  }

  resetForm(): void {
    this.formProduct = { name: '', description: '', quantity: 0, price: 0 };
  }

  onSubmit(): void {
    if (this.formProduct.id) {
      this.productService.updateProduct(this.formProduct.id, this.formProduct).subscribe(() => {
        this.productSaved.emit();
        this.resetForm();
      });
    } else {
      this.productService.createProduct(this.formProduct).subscribe(() => {
        this.productSaved.emit();
        this.resetForm();
      });
    }
  }

}
