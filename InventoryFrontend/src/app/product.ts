import { Component, OnInit } from '@angular/core';
import { ProductService, Product } from './product.spec';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  products: Product[] = [];
  selectedProduct: Product = { name: '', description: '', quantity: 0 };

  constructor(private productService: ProductService) {}

  ngOnInit() {
    this.loadProducts();
  }

  loadProducts() {
    this.productService.getAll().subscribe(data => this.products = data);
  }

  selectProduct(product: Product) {
    this.selectedProduct = { ...product };
  }

  saveProduct() {
    if (this.selectedProduct.id) {
      this.productService.update(this.selectedProduct.id, this.selectedProduct)
        .subscribe(() => this.resetAndReload());
    } else {
      this.productService.create(this.selectedProduct)
        .subscribe(() => this.resetAndReload());
    }
  }

  deleteProduct(id: number) {
    this.productService.delete(id).subscribe(() => this.loadProducts());
  }

  resetAndReload() {
    this.selectedProduct = { name: '', description: '', quantity: 0 };
    this.loadProducts();
  }
}