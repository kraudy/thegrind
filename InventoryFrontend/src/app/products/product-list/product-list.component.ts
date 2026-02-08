import { Component, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ProductService } from '../product.service';
import { Product } from '../product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-list.html',
  styleUrls: ['./product-list.css'],
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  selectedProduct: Product | null = null;

  constructor(
    private productService: ProductService,
    private cd: ChangeDetectorRef 
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }
 
  // Load list
  loadProducts(): void {
    this.productService.getAll().subscribe({
      next: (data) => {
        this.products = data || [];
        console.log('[ProductList] loaded products:', this.products.length, this.products);
        this.cd.detectChanges(); // force view update if zone isn't triggering it
      },
      error: (err) => {
        // log full error so you can inspect status/code in the console
        console.error('[ProductList] failed to load products', err);
        this.products = [];
        if (err?.status === 0) {
          console.log('Cannot reach backend (network error). Is the Spring server running?');
        } else if (err?.status) {
          console.log(`Backend error ${err.status}: ${err?.message || err?.statusText || 'unknown'}`);
        } else {
          console.log('Unexpected error loading products (check console)');
        }
      }
    });
  }

  selectProduct(product: Product): void {
    this.selectedProduct = {...product};
  }

  deleteProduct(id?: number): void {
    if (id && confirm('¿Seguro desea eliminar?')){
      this.productService.delete(id).subscribe(() => this.loadProducts());
    }
  }

}
