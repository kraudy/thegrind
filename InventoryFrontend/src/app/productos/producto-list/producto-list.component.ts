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
    private productoSubTipoService: ProductoSubTipoService,
    private productoMedidaService: ProductoMedidaService,
    private productoModeloService: ProductoModeloService,
    private productoColorService: ProductoColorService,
    private router: Router,
    private cd: ChangeDetectorRef 
  ) {}

  ngOnInit(): void {
    this.loadTipos();
    this.loadSubTipos();
    this.loadMedidas();
    this.loadModelos(); 
    this.loadColores();
    this.loadProducts();
  }
 
  // Load list
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

  private loadSubTipos(): void {
    this.productoSubTipoService.getAll().subscribe({
      next: (data) => {
        this.subTipos = data || [];
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[ProductList] failed to load sub-tipos', err);
        this.subTipos = [];
        this.cd.detectChanges();
      }
    });
  }

  private loadMedidas(): void {
    this.productoMedidaService.getAll().subscribe({
      next: (data) => {
        this.medidas = data || [];
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[ProductList] failed to load medidas', err);
        this.medidas = [];
        this.cd.detectChanges();
      }
    });
  }

  private loadModelos(): void {
    this.productoModeloService.getAll().subscribe({
      next: (data) => {
        this.modelos = data || [];
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[ProductList] failed to load modelos', err);
        this.modelos = [];
        this.cd.detectChanges();
      }
    });
  }

  private loadColores(): void {
    this.productoColorService.getAll().subscribe({
      next: (data) => {
        this.colores = data || [];
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[ProductList] failed to load colores', err);
        this.colores = [];
        this.cd.detectChanges();
      }
    });
  }

  onFilterChange(): void {
    this.loadProducts();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedTipo = '';
    this.selectedSubTipo = '';
    this.selectedMedida = '';
    this.selectedModelo = '';
    this.selectedColor = '';   
    this.sinPrecio = false;
    this.loadProducts();
  }


  viewDetails(id: number): void {
    this.router.navigate(['/productos-detalle', id]);
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
