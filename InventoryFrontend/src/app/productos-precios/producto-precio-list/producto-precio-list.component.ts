import { Component, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ProductoPrecio } from '../producto-precio.model';
import { ProductoPrecioService } from '../producto-precio.service';

@Component({
  selector: 'app-producto-precio-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './producto-precio-list.html',
  styleUrls: ['./producto-precio-list.css'],
})
export class ProductoPrecioListComponent {
  productos: ProductoPrecio[] = [];
    selectedProductoPrecio: ProductoPrecio | null = null;
  
    constructor(
      private productoPrecioService: ProductoPrecioService,
      private cd: ChangeDetectorRef 
    ) {}
  
    ngOnInit(): void {
      this.loadProducts();
    }
   
    // Load list
    loadProducts(): void {
      this.productoPrecioService.getAll().subscribe({
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
  
    selectProduct(product: ProductoPrecio): void {
      this.selectedProductoPrecio = {...product};
    }
  
    deleteProduct(producto: ProductoPrecio): void {
      if (confirm('¿Seguro desea eliminar?')) {
        this.productoPrecioService.deleteComposite(producto.productoId, producto.precio)
          .subscribe(() => this.loadProducts());
      }
    }
}
