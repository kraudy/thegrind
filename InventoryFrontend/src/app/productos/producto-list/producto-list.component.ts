import { Component, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ProductoService } from '../producto.service';
import { Producto } from '../producto.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './producto-list.html',
  styleUrls: ['./producto-list.css'],
})
export class ProductoListComponent implements OnInit {
  productos: Producto[] = [];
  selectedProduct: Producto | null = null;

  constructor(
    private productoService: ProductoService,
    private cd: ChangeDetectorRef 
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }
 
  // Load list
  loadProducts(): void {
    this.productoService.getAll().subscribe({
      next: (data) => {
        this.productos = data || [];
        console.log('[ProductList] loaded products:', this.productos.length, this.productos);
        this.cd.detectChanges(); // force view update if zone isn't triggering it
      },
      error: (err) => {
        // log full error so you can inspect status/code in the console
        console.error('[ProductList] failed to load products', err);
        this.productos = [];
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

  selectProduct(product: Producto): void {
    this.selectedProduct = {...product};
  }

  deleteProduct(id?: number): void {
    if (id && confirm('¿Seguro desea eliminar?')){
      this.productoService.delete(id).subscribe(() => this.loadProducts());
    }
  }

}
