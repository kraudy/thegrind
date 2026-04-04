import { Component, Input, Output, EventEmitter, OnChanges, OnInit, ChangeDetectorRef } from '@angular/core';

import { CommonModule, Location } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

import { OrdenDetalleService } from '../orden-detalle.service';
import { OrdenDetalle } from '../orden-detalle.model';

import { ProductoService } from '../../productos/producto.service';
import { Producto } from '../../productos/producto.model';
import { ProductoPrecioService } from '../../productos-precios/producto-precio.service';
import { ProductoPrecio } from '../../productos-precios/producto-precio.model';

@Component({
  selector: 'app-orden-detalle-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orden-detalle-form.html',
  styleUrls: ['./orden-detalle-form.css'],
})
export class OrdenDetalleFormComponent implements OnChanges, OnInit {
  @Input() ordenDetalle: OrdenDetalle | null = null;
  @Output() ordenDetalleSaved = new EventEmitter<void>();

  isEdit = false;
  clienteNombre: string = '';

  constructor(
    private ordenDetalleService: OrdenDetalleService,
    private productoService: ProductoService,
    private productoPrecioService: ProductoPrecioService,
    private location: Location,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef
  ) {}

  formOrdenDetalle: Partial<OrdenDetalle> = { 
    idOrden: 0,
    idOrdenDetalle: 0,
    idProducto: 0,
    cantidad: 0,
    precioUnitario: 0,
    subtotal: 0
  };

  searchProducto: string = '';
  productosFiltrados: Producto[] = [];
  private searchTerms = new Subject<string>();

  // Precios del producto seleccionado
  preciosDisponibles: ProductoPrecio[] = [];

  ngOnInit(): void {
    const params = this.route.snapshot.paramMap;
    const queryParams = this.route.snapshot.queryParams;

    this.clienteNombre = queryParams['clienteNombre'] || '';

    const ordenIdStr = params.get('ordenId');
    if (!ordenIdStr) {
      console.error('No se encontró ordenId en la ruta');
      return;
    }

    this.formOrdenDetalle.idOrden = Number(ordenIdStr);

    
    if (isNaN(this.formOrdenDetalle.idOrden) || this.formOrdenDetalle.idOrden <= 0) {
      console.error('ordenId inválido:', this.formOrdenDetalle.idOrden);
      alert('ID de orden inválido. Regresando...');
    }

    const idOrdenDetalleStr = params.get('idOrdenDetalle');

    if (idOrdenDetalleStr) {
      // Modo edición
      const linea = Number(idOrdenDetalleStr);

      this.ordenDetalleService.getById(this.formOrdenDetalle.idOrden, linea).subscribe({
        next: (data) => {
          this.loadForm(data);
          this.isEdit = true;
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error('[OrdenDetalleForm] Error cargando detalle para edición', err);
          this.resetForm();
          this.isEdit = false;
        }
      });
    } else {
      // Modo creación
      this.resetForm();  // conserva el idOrden
      this.isEdit = false;
    }
  }

  ngOnChanges(): void {
    if (this.ordenDetalle) {
      this.loadForm(this.ordenDetalle);
      this.isEdit = true;
    }
  }

  private loadForm(data: OrdenDetalle): void {
    this.formOrdenDetalle = { ...data };
    this.updateSubtotal();

    if (this.formOrdenDetalle.idProducto) {
      // Cargar nombre del producto para mostrar en modo edición (backend)
      this.productoService.getById(this.formOrdenDetalle.idProducto).subscribe({
        next: (prod) => {
          this.searchProducto = `${prod.id} - ${prod.nombre}`;
          this.cd.detectChanges();
        },
        error: () => {
          this.searchProducto = `ID: ${this.formOrdenDetalle.idProducto} (no encontrado)`;
        }
      });

      // Cargar precios pero SIN auto-seleccionar el primero (para no pisar el precio guardado)
      this.loadPreciosForProducto(this.formOrdenDetalle.idProducto, false);
    }
  }

  onSearchChange(newValue: string): void {
    const term = (newValue || '').trim();

    // Clear suggestions if input is empty
    if (!term) {
      this.productosFiltrados = [];
      return;
    }

    // Intelligent search: if it's a number → exact ID, else → nombre LIKE
    // (exactly the same logic used in ProductoListComponent)
    let filterId: number | undefined;
    let filterNombre: string | undefined;

    const num = Number(term);
    if (!isNaN(num) && num > 0 && Number.isInteger(num)) {
      filterId = num;
    } else {
      filterNombre = term;
    }

    this.productoService.getAllWithFilters({
      id: filterId,
      nombre: filterNombre,
      // tipo / subTipo / sinPrecio are not needed for this autocomplete,
      // but you can add them later if you want extra filters in the form
    }).subscribe({
      next: (data) => {
        this.productosFiltrados = data || [];
        // Optional: limit the dropdown (recommended for UX)
        // this.productosFiltrados = (data || []).slice(0, 15);
      },
      error: (err) => {
        console.error('[OrdenDetalleForm] failed to search products', err);
        this.productosFiltrados = [];
      }
    });
  }

  seleccionarProducto(producto: Producto): void {
    //this.formOrdenDetalle.idProducto = producto.id;
    //this.searchProducto = `${producto.id} - ${producto.nombre}`;
    //this.productosFiltrados = [];

    this.formOrdenDetalle.idProducto = producto.id;

    // Show a friendly value in the search input
    this.searchProducto = `${producto.id} - ${producto.nombre}`;

    // Hide the suggestion list
    this.productosFiltrados = [];

    this.loadPreciosForProducto(producto.id); // auto-selecciona el primer precio (modo creación)
  }

  private loadPreciosForProducto(idProducto: number, autoSelect = true): void {
    this.preciosDisponibles = []; // clear previous

    this.productoPrecioService.getPreciosByProducto(idProducto).subscribe({
      next: (precios) => {
        // Mantener solo los activos (igual que antes)
        this.preciosDisponibles = precios; // no fitler, this can be done in the backend

        if (autoSelect && this.preciosDisponibles.length > 0) {
          this.formOrdenDetalle.precioUnitario = this.preciosDisponibles[0].precio;
          this.updateSubtotal();
        }
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando precios para producto', idProducto, err);
        this.preciosDisponibles = [];
      }
    });
  }

  goBack(): void {
    this.location.back();
  }

  updateSubtotal(): void {
    const cantidad = this.formOrdenDetalle.cantidad || 0;
    const precio = this.formOrdenDetalle.precioUnitario || 0;
    this.formOrdenDetalle.subtotal = cantidad * precio;
    console.log('Subtotal actualizado →', this.formOrdenDetalle.subtotal); // para debug
    this.cd.detectChanges();
  }

  resetForm(): void {
    const currentOrdenId = this.formOrdenDetalle.idOrden ?? 0;
    this.formOrdenDetalle = {
      idOrden: currentOrdenId,
      idOrdenDetalle: 0,
      idProducto: 0,
      cantidad: 0,
      precioUnitario: 0,
      subtotal: 0
    };

    this.searchProducto = '';
    this.productosFiltrados = [];
    this.preciosDisponibles = [];
  }

  /** Helper: devuelve el objeto ProductoPrecio que coincide con el precio seleccionado */
  private getSelectedPrecio(): ProductoPrecio | undefined {
    if (!this.formOrdenDetalle.precioUnitario) return undefined;
    return this.preciosDisponibles.find(
      p => p.precio === this.formOrdenDetalle.precioUnitario
    );
  }

  onSubmit(): void {
    const selectedPrecio = this.getSelectedPrecio();

    if ((this.formOrdenDetalle.cantidad || 0) <= 0) {
      alert('La cantidad debe ser mayor a cero.');
      return; // ← detiene el envío
    }

    if (selectedPrecio && selectedPrecio.cantidadRequerida > 0) {
      const cantidadUsuario = this.formOrdenDetalle.cantidad || 0;

      if (cantidadUsuario < selectedPrecio.cantidadRequerida) {
        alert(
          `La cantidad debe ser al menos ${selectedPrecio.cantidadRequerida} ` +
          `para el precio seleccionado (${selectedPrecio.precio}).`
        );
        return; // ← detiene el envío
      }
    }

    if (this.isEdit) {
      const updatePayload = {
        cantidad: this.formOrdenDetalle.cantidad,
        precioUnitario: this.formOrdenDetalle.precioUnitario,
        subtotal: this.formOrdenDetalle.subtotal
      };

      this.ordenDetalleService.update(
        this.formOrdenDetalle.idOrden!,
        this.formOrdenDetalle.idOrdenDetalle!,
        updatePayload
      ).subscribe({
        next: () => {
          this.ordenDetalleSaved.emit();
          this.goBack();
        },
        error: (err) => console.error('Error actualizando detalle', err)
      });

    } else {
      const createPayload = {
        cantidad: this.formOrdenDetalle.cantidad,
        precioUnitario: this.formOrdenDetalle.precioUnitario,
        subtotal: this.formOrdenDetalle.subtotal
      };

      this.ordenDetalleService.create(this.formOrdenDetalle.idOrden!, this.formOrdenDetalle.idProducto!, createPayload).subscribe({
        next: () => {
          this.ordenDetalleSaved.emit();
          this.goBack();
        },
        error: (err) => console.error('Error creando detalle', err)
      });
    }
  }
}