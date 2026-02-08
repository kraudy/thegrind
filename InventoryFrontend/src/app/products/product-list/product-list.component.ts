import { Component, OnInit } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ProductService } from '../product.service';
import { Product } from '../product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-list.html',
  styleUrl: './product-list.css',
})
export class ProductListComponent {
  products: Product[] = [];
  selectedProduct: Product | null = null;

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.loadProducts();
  }
 
  // Load list
  loadProducts(): void {
    this.productService.getAll().subscribe(data => this.products = data);
    console.log('Loading products: ' + this.products.length);
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
