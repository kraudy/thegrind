import { Component, Input, Output, EventEmitter, OnChanges, OnInit, ChangeDetectorRef} from '@angular/core';

import { CommonModule, Location  } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Producto } from '../../productos/producto.model';
import { ProductoService } from '../../productos/producto.service';

import { ProductoPrecioService } from '../producto-precio.service';
import { ProductoPrecio } from '../producto-precio.model';

import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';


@Component({
  selector: 'app-producto-precio-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './producto-precio-form.html',
  styleUrls: ['./producto-precio-form.css'],
})
export class ProductoPrecioFormComponent  implements OnChanges, OnInit {
  @Input() productoPrecio: ProductoPrecio | null = null;
  @Output() productoPrecioSaved = new EventEmitter<void>();
  isEdit = false;
  productoPrecioId: number | null = null;

  constructor(
    private productoPrecioService: ProductoPrecioService,
    private productoService: ProductoService,
    private location: Location,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef
  ) {}

  formProductoPrecio: Partial<ProductoPrecio> = { precio: 0.00, descripcion: '', cantidadRequerida: 0};

  // Nuevas propiedades para búsqueda
  filteredProductos: Producto[] = [];
  selectedProducto: Producto | null = null;
  searchTerm: string = '';
  private searchTerms = new Subject<string>();

  ngOnInit(): void {
    // Configuración del debounce para búsqueda
    this.searchTerms.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => term ? this.productoService.search(term) : of([]))
    ).subscribe(productos => {
      this.filteredProductos = productos;
      this.cd.detectChanges();
    });

    if (!this.productoPrecio) { // If no id in route — ensure fresh form
      this.resetForm();
      this.isEdit = false;
      console.log('[ProductoPrecioForm] no productoPrecio provided', this.productoPrecio);
    }

    const productoIdParam = this.route.snapshot.paramMap.get('productoId');
    const precioParam = this.route.snapshot.paramMap.get('precio');

    if (!productoIdParam || !precioParam) return;
    const productoId = Number(productoIdParam);
    const precio = parseFloat(precioParam); // BigDecimal → number
    if (isNaN(productoId) || isNaN(precio)) return;

    this.productoPrecioService.getByComposite(productoId, precio).subscribe({
      next: (data) => {
        this.formProductoPrecio = { ...data };
        this.isEdit = true;
        this.loadProductoForDisplay(); // Cargar nombre del producto en modo edición
        console.log('[ProductoPrecioForm] loaded producto for edit', data);
        this.cd.detectChanges();  // Force view update
      },
      error: (err) => {
        console.error('[ProductoPrecioForm] failed to load producto', err);
        // Keep form reset so user can create, but mark not edit on failure
        this.resetForm();
        this.isEdit = false;
        this.cd.detectChanges();  // Force view update
      }
    });
  }

  ngOnChanges(): void {
    if (this.productoPrecio) {
      this.formProductoPrecio = { ...this.productoPrecio };
      this.isEdit = !!this.productoPrecio.productoId;  // true if editing an existing producto
      if (this.isEdit) this.loadProductoForDisplay();
    } else {
      this.resetForm();
      this.isEdit = false;
    }
  }

  private loadProductoForDisplay(): void {
    if (!this.formProductoPrecio.productoId) return;
    this.productoService.getById(this.formProductoPrecio.productoId!).subscribe({
      next: (prod) => {
        this.selectedProducto = prod;
        this.cd.detectChanges();
      },
      error: () => {
        this.selectedProducto = null;
        this.cd.detectChanges();
      }
    });
  }

  onSearchChange(term: string): void {
    if (this.isEdit) return; // No buscar en modo edición

    this.searchTerm = term;
    if (!term.trim()) {
      this.selectedProducto = null;
      this.formProductoPrecio.productoId = undefined;
      this.filteredProductos = [];
    }
    this.searchTerms.next(term.trim());
  }

  selectProducto(producto: Producto): void {
    if (this.isEdit) return; // No permitir cambio en edición

    this.selectedProducto = producto;
    this.formProductoPrecio.productoId = producto.id;
    this.searchTerm = `${producto.nombre} (ID: ${producto.id})`;
    this.filteredProductos = []; // Ocultar dropdown
    this.cd.detectChanges();
  }

  goBack(): void {
    this.location.back();
  }

  resetForm(): void {
    this.formProductoPrecio = { precio: 0.00, descripcion: '', cantidadRequerida: 0};
    this.searchTerm = '';
    this.selectedProducto = null;
    this.filteredProductos = [];
    this.formProductoPrecio.productoId = undefined;
  }

  onSubmit(): void {
    if (this.isEdit) {
      console.log('Updating producto precio: ' + this.formProductoPrecio.productoId);
      const originalPrecio = this.formProductoPrecio.precio!;
      this.productoPrecioService.updateComposite(this.formProductoPrecio.productoId!, originalPrecio, this.formProductoPrecio).subscribe(() => {
        this.productoPrecioSaved.emit();
        this.resetForm();
        this.location.back();
      });
    } else {
      console.log('Creating producto precio: ');
      this.productoPrecioService.create(this.formProductoPrecio as ProductoPrecio).subscribe(() => {
        this.productoPrecioSaved.emit();
        this.resetForm();
        this.location.back();
      });
    }
  }
}
