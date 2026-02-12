import { Component, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ProductoTipoService } from '../producto-tipo.service';
import { ProductoTipo } from '../producto-tipo.model'
import { Producto } from '../../productos/producto.model';


@Component({
  selector: 'app-producto-tipo-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './producto-tipo-list.html',
  styleUrls: ['./producto-tipo-list.css'],
})
export class ProductoTipoListComponent implements OnInit {
  productosTipos: ProductoTipo[] = [];
  selectedProductoTipo: ProductoTipo | null = null;
  
    constructor(
      private productoTipoService: ProductoTipoService,
      private cd: ChangeDetectorRef 
    ) {}
  
    ngOnInit(): void {
      this.loadProducts();
    }
   
    // Load list
    loadProducts(): void {
      this.productoTipoService.getAll().subscribe({
        next: (data) => {
          this.productosTipos = data || [];
          console.log('[ProductosTipoList] loaded products:', this.productosTipos.length, this.productosTipos);
          this.cd.detectChanges(); // force view update if zone isn't triggering it
        },
        error: (err) => {
          // log full error so you can inspect status/code in the console
          console.error('[ProductosTipoList] failed to load products', err);
          this.productosTipos = [];
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
  
    selectProduct(productoTipo: ProductoTipo): void {
      this.selectedProductoTipo = {...productoTipo};
    }
  
    deleteProduct(tipo: string): void {
      if (tipo && confirm('¿Seguro desea eliminar?')){
        this.productoTipoService.delete(tipo).subscribe(() => this.loadProducts());
      }
    }
}
