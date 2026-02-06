import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface Product {
  id?: number;
  name: string;
  description: string;
  quantity: number;
}

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [FormsModule],  // Needed for ngModel
  templateUrl: './products.html',
  styleUrl: './products.css'  // if you have one
})
export class Products {
  products: Product[] = [
    { id: 1, name: 'Laptop', description: 'Gaming laptop', quantity: 5 },
    { id: 2, name: 'Mouse', description: 'Wireless mouse', quantity: 20 }
  ];

  selectedProduct: Product = { name: '', description: '', quantity: 0 };

  selectProduct(product: Product) {
    this.selectedProduct = { ...product };
  }

  saveProduct() {
    if (this.selectedProduct.id) {
      // Update existing
      const index = this.products.findIndex(p => p.id === this.selectedProduct.id);
      if (index !== -1) {
        this.products[index] = { ...this.selectedProduct };
      }
    } else {
      // Add new
      const newId = this.products.length ? Math.max(...this.products.map(p => p.id!)) + 1 : 1;
      this.products.push({ ...this.selectedProduct, id: newId });
    }
    this.selectedProduct = { name: '', description: '', quantity: 0 };
  }

  deleteProduct(id?: number) {
    if (id) {
      this.products = this.products.filter(p => p.id !== id);
    }
  }
}