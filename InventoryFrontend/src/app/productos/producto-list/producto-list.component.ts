import { Component, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router'; 

import { FormsModule } from '@angular/forms';

import { ProductoService } from '../producto.service';
import { Producto } from '../producto.model';

import { ProductoTipo } from '../../productos-tipos/producto-tipo.model';
import { ProductoSubTipo } from '../../productos-sub-tipos/producto-sub-tipo.model';

import { ProductoSubTipoService } from '../../productos-sub-tipos/producto-sub-tipo.service';
import { ProductoTipoService } from '../../productos-tipos/producto-tipo.service';

import { ProductoMedida } from '../../productos-medidas/producto-medida.model';
import { ProductoMedidaService } from '../../productos-medidas/producto-medida.service';

import { ProductoModelo } from '../../productos-modelos/producto-modelo.model';
import { ProductoModeloService } from '../../productos-modelos/producto-modelo.service';

import { ProductoColor } from '../../productos-colores/producto-color.model';
import { ProductoColorService } from '../../productos-colores/producto-color.service';

import { ProductoConfigService } from '../../productos-config/producto-config.service';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './producto-list.html',
  styleUrls: ['./producto-list.css'],
})
export class ProductoListComponent implements OnInit {
  productos: Producto[] = [];
  selectedProduct: Producto | null = null;

  loading = false;
  errorMessage = '';

  // Filters
  searchTerm = '';
  selectedTipo = '';
  selectedSubTipo = '';
  selectedMedida = '';
  selectedModelo = ''; 
  selectedColor = '';
  sinPrecio = false;   

  tipos: ProductoTipo[] = [];
  medidas: ProductoMedida[] = [];
  modelos: ProductoModelo[] = [];
  subTipos: ProductoSubTipo[] = [];
  colores: ProductoColor[] = []; 

  constructor(
    private productoService: ProductoService,
    private productoTipoService: ProductoTipoService,
    private productoConfigService: ProductoConfigService,
    private router: Router,
    private cd: ChangeDetectorRef,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadTipos();
    this.refreshConfig(); // ← initial load of ALL valid attribute combinations
  }
 
  // Load list (unchanged – now triggered by refreshConfig)
  loadProducts(): void {
    this.loading = true;
    this.errorMessage = '';

    // Intelligent search: if it's a number → exact ID, else → nombre LIKE
    let filterId: number | undefined;
    let filterNombre: string | undefined;

    const term = this.searchTerm.trim();
    if (term) {
      const num = Number(term);
      if (!isNaN(num) && num > 0 && Number.isInteger(num)) {
        filterId = num;
      } else {
        filterNombre = term;
      }
    }

    this.productoService.getAllWithFilters({
      id: filterId,
      nombre: filterNombre,
      tipo: this.selectedTipo || undefined,
      subTipo: this.selectedSubTipo || undefined,
      medida: this.selectedMedida || undefined,
      modelo: this.selectedModelo || undefined,
      color: this.selectedColor || undefined,
      sinPrecio: this.sinPrecio
    }).subscribe({
      next: (data) => {
        this.productos = data || [];
        this.loading = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[ProductList] failed to load products', err);
        this.productos = [];
        this.loading = false;
        this.errorMessage = 'No se pudo cargar los productos. Verifique que el backend esté corriendo.';
        this.cd.detectChanges();
      }
    });
  }

  private loadTipos(): void {
    this.productoTipoService.getAll().subscribe({
      next: (data) => {
        this.tipos = data || [];
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[ProductList] failed to load tipos', err);
        this.tipos = [];
        this.cd.detectChanges();
      }
    });
  }

  /** 
   * When any attribute is selected, the backend returns ONLY valid combinations for the other dropdowns.
   */
  private refreshConfig(): void {
    this.productoConfigService.getConfig(
      this.selectedTipo || undefined,
      this.selectedSubTipo || undefined,
      this.selectedMedida || undefined,
      this.selectedModelo || undefined,
      this.selectedColor || undefined
    ).subscribe({
      next: (config) => {
        // Build dynamic dropdown options from valid combinations only
        this.subTipos = [...new Set(config.map(c => c.subTipo))]
          .map(st => ({ subTipo: st, descripcion: '' } as ProductoSubTipo));

        this.medidas = [...new Set(config.map(c => c.medida))]
          .map(m => ({ medida: m, descripcion: '' } as ProductoMedida));

        this.modelos = [...new Set(config.map(c => c.modelo))]
          .map(mod => ({ modelo: mod, descripcion: '' } as ProductoModelo));

        this.colores = [...new Set(config.map(c => c.color))]
          .map(col => ({ color: col, descripcion: '' } as ProductoColor));

        this.cd.detectChanges();

        // Auto-clean any selection that is no longer valid after the filter change
        let anyReset = false;

        if (this.selectedSubTipo && !this.subTipos.some(s => s.subTipo === this.selectedSubTipo)) {
          this.selectedSubTipo = '';
          anyReset = true;
        }
        if (this.selectedMedida && !this.medidas.some(m => m.medida === this.selectedMedida)) {
          this.selectedMedida = '';
          anyReset = true;
        }
        if (this.selectedModelo && !this.modelos.some(mod => mod.modelo === this.selectedModelo)) {
          this.selectedModelo = '';
          anyReset = true;
        }
        if (this.selectedColor && !this.colores.some(col => col.color === this.selectedColor)) {
          this.selectedColor = '';
          anyReset = true;
        }

        if (anyReset) {
          this.refreshConfig();   // re-run with cleaned values (recursive, safe)
        } else {
          this.loadProducts();    // ← refresh the product list with current (valid) filters
        }
      },
      error: (err) => {
        console.error('[ProductoList] failed to load config', err);
        // Fallback: clear dynamic lists so user can still search without filters
        this.subTipos = [];
        this.medidas = [];
        this.modelos = [];
        this.colores = [];
        this.loadProducts();
      }
    });
  }

  onFilterChange(): void {
    this.refreshConfig();   // ← now triggers the full cascading logic
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedTipo = '';
    this.selectedSubTipo = '';
    this.selectedMedida = '';
    this.selectedModelo = '';
    this.selectedColor = '';   
    this.sinPrecio = false;
    this.refreshConfig();   // ← uses the new dynamic logic
  }

  viewDetails(id: number): void {
    this.router.navigate(['/productos-detalle', id]);
  }

  selectProduct(product: Producto): void {
    this.selectedProduct = {...product};
  }

  deleteProduct(id?: number): void {
    if (id && confirm('¿Seguro desea eliminar?')){
      this.productoService.delete(id).subscribe({
        next: () => this.loadProducts(),
        error: (err) => {
          console.error('[ProductList] failed to delete product', err);
          this.toastService.showToast(
            'error',
            'Error al eliminar',
            err?.error?.message || 'No se pudo eliminar el producto.',
            6000
          );
        }
      });
    }
  }


}
