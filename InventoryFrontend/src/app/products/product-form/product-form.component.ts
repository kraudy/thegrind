import { Component, Input, Output, EventEmitter, OnChanges, OnInit, ChangeDetectorRef} from '@angular/core';

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
export class ProductFormComponent implements OnChanges, OnInit{
  @Input() product: Product | null = null;
  @Output() productSaved = new EventEmitter<void>();
  isEdit = false;
  productId: number | null = null;

  constructor(
    private productService: ProductService,
    private location: Location,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef
  ) {}

  formProduct: Product = { name: '', description: '', quantity: 0};

  ngOnInit(): void {
    if (!this.product) { // If no id in route — ensure fresh form
      this.resetForm();
      this.isEdit = false;
    }
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {return;}
    const id = Number(idParam);
    if (isNaN(id)) {return;}
    this.productId = id;
    this.productService.getById(id).subscribe({
      next: (data) => {
        this.formProduct = { ...data };
        this.isEdit = true;
        console.log('[ProductForm] loaded product for edit', data);
        this.cd.detectChanges();  // Force view update
      },
      error: (err) => {
        console.error('[ProductForm] failed to load product', err);
        // Keep form reset so user can create, but mark not edit on failure
        this.resetForm();
        this.isEdit = false;
        this.cd.detectChanges();  // Force view update
      }
    });
  }

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
        this.location.back();
      });
    } else {
      console.log('Creating product: ');
      this.productService.create(this.formProduct).subscribe(() => {
        this.productSaved.emit();
        this.resetForm();
        this.location.back();
      });
    }
  }

}
