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

  // Búsqueda de productos (ahora con backend + debounce)
  searchProducto: string = '';
  productosFiltrados: Producto[] = [];
  private searchTerms = new Subject<string>();

  // Precios del producto seleccionado
  allPrecios: ProductoPrecio[] = [];
  preciosDisponibles: ProductoPrecio[] = [];

  ngOnInit(): void {
    const params = this.route.snapshot.paramMap;

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

    // Cargar SOLO los precios (los productos ahora se buscan en backend)
    this.cargarDatosIniciales();

    // Configuración del buscador con debounce (exactamente como el ejemplo que me diste)
    this.searchTerms.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => this.productoService.search(term))  // backend search
    ).subscribe(productos => {
      this.productosFiltrados = productos;
      this.cd.detectChanges();
    });

    const idOrdenDetalleStr = params.get('idOrdenDetalle');
    const idProductoStr = params.get('idProducto');

    if (idOrdenDetalleStr && idProductoStr) {
      // Modo edición
      const linea = Number(idOrdenDetalleStr);
      const prodId = Number(idProductoStr);

      this.ordenDetalleService.getByCompositeKey(this.formOrdenDetalle.idOrden, linea, prodId).subscribe({
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

  private cargarDatosIniciales(): void {
    this.productoPrecioService.getAll().subscribe({
      next: (precios) => {
        this.allPrecios = precios.filter(p => p.activo);
      },
      error: (err) => console.error('Error cargando precios iniciales', err)
    });
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

  // Nuevo método (igual que en el ejemplo que me pasaste)
  onSearchChange(term: string): void {
    if (this.isEdit) return; // nunca debería llamarse en edición por el *ngIf

    this.searchProducto = term;

    if (!term || term.trim().length < 2) {
      this.productosFiltrados = [];
      return;
    }

    this.searchTerms.next(term.trim());
  }

  seleccionarProducto(producto: Producto): void {
    this.formOrdenDetalle.idProducto = producto.id;
    this.searchProducto = `${producto.id} - ${producto.nombre}`;
    this.productosFiltrados = [];

    this.loadPreciosForProducto(producto.id); // auto-selecciona el primer precio (modo creación)
  }

  private loadPreciosForProducto(idProducto: number, autoSelect = true): void {
    this.preciosDisponibles = this.allPrecios.filter(p => p.productoId === idProducto);

    if (autoSelect && this.preciosDisponibles.length > 0) {
      this.formOrdenDetalle.precioUnitario = this.preciosDisponibles[0].precio;
      this.updateSubtotal();
    }
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

  onSubmit(): void {
    if (this.isEdit) {
      const updatePayload = {
        cantidad: this.formOrdenDetalle.cantidad,
        precioUnitario: this.formOrdenDetalle.precioUnitario,
        subtotal: this.formOrdenDetalle.subtotal
      };

      this.ordenDetalleService.update(
        this.formOrdenDetalle.idOrden!,
        this.formOrdenDetalle.idOrdenDetalle!,
        this.formOrdenDetalle.idProducto!,
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