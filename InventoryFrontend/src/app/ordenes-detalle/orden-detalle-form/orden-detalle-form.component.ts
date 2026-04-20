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

import { ProductoTipo } from '../../productos-tipos/producto-tipo.model';
import { ProductoSubTipo } from '../../productos-sub-tipos/producto-sub-tipo.model';
import { ProductoTipoService } from '../../productos-tipos/producto-tipo.service';
import { ProductoSubTipoService } from '../../productos-sub-tipos/producto-sub-tipo.service';

import { ProductoColor } from '../../productos-colores/producto-color.model';
import { ProductoModelo } from '../../productos-modelos/producto-modelo.model';
import { ProductoMedida } from '../../productos-medidas/producto-medida.model';

import { ProductoConfigService } from '../../productos-config/producto-config.service';

import { NotificationService } from '../../shared/notification.service';
import { ToastService } from '../../shared/toast/toast.service';

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

  // Dynamic filter dropdowns (valid combinations only)
  tipos: ProductoTipo[] = [];
  subTipos: ProductoSubTipo[] = [];
  medidas: ProductoMedida[] = [];
  modelos: ProductoModelo[] = [];
  colores: ProductoColor[] = [];

  // Selected filter values
  selectedTipo: string = '';
  selectedSubTipo: string = '';
  selectedMedida: string = '';
  selectedModelo: string = '';
  selectedColor: string = '';

  constructor(
    private ordenDetalleService: OrdenDetalleService,
    private productoService: ProductoService,
    private productoPrecioService: ProductoPrecioService,
    private productoTipoService: ProductoTipoService,
    private productoSubTipoService: ProductoSubTipoService,
    private productoConfigService: ProductoConfigService,
    private location: Location,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef,
    private toastService: ToastService
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
      this.toastService.showToast('error', 'ID inválido', 'ID de orden inválido. Regresando...', 6000);
      this.goBack();
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
      this.refreshConfig(); 
    }

    // Load filter options (always - harmless in edit mode)
    this.loadTipos();
  }

  ngOnChanges(): void {
    if (this.ordenDetalle) {
      this.loadForm(this.ordenDetalle);
      this.isEdit = true;
    }
  }

  private loadTipos(): void {
    this.productoTipoService.getAll().subscribe({
      next: (data) => { this.tipos = data || []; this.cd.detectChanges(); },
      error: (err) => { console.error('[OrdenDetalleForm] failed to load tipos', err); this.tipos = []; }
    });
  }

  private refreshConfig(): void {
    this.productoConfigService.getConfig(
      this.selectedTipo || undefined,
      this.selectedSubTipo || undefined,
      this.selectedMedida || undefined,
      this.selectedModelo || undefined,
      this.selectedColor || undefined
    ).subscribe({
      next: (config) => {
        this.subTipos = [...new Set(config.map(c => c.subTipo))]
          .map(st => ({ subTipo: st, descripcion: '' } as ProductoSubTipo));

        this.medidas = [...new Set(config.map(c => c.medida))]
          .map(m => ({ medida: m, descripcion: '' } as ProductoMedida));

        this.modelos = [...new Set(config.map(c => c.modelo))]
          .map(mod => ({ modelo: mod, descripcion: '' } as ProductoModelo));

        this.colores = [...new Set(config.map(c => c.color))]
          .map(col => ({ color: col, descripcion: '' } as ProductoColor));

        this.cd.detectChanges();

        // Auto-clean any selection that is no longer valid
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
          this.refreshConfig();   // re-run with cleaned values
        } else {
          this.performProductSearch();   // ← refresh product list
        }
      },
      error: (err) => console.error('[OrdenDetalleForm] failed to load config', err)
    });
  }

  onAnyFilterChange(): void {
    this.refreshConfig();
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

  // Centralized search with filters + intelligent ID/name logic
  private performProductSearch(): void {
    const term = (this.searchProducto || '').trim();

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
      tipo: this.selectedTipo || undefined,
      subTipo: this.selectedSubTipo || undefined,
      medida: this.selectedMedida || undefined,
      modelo: this.selectedModelo || undefined,
      color: this.selectedColor || undefined
    }).subscribe({
      next: (data) => {
        this.productosFiltrados = data || [];
        if (this.productosFiltrados.length === 0 && term) {
          this.toastService.showToast('warning', 'Sin resultados', 'No se encontraron productos que coincidan con la búsqueda', 5000);
        }
        this.cd.detectChanges(); 
      },
      error: (err) => {
        console.error('[OrdenDetalleForm] failed to search products', err);
        this.productosFiltrados = [];
        this.toastService.showToast('error', 'Error en búsqueda', 'Ocurrió un error al buscar productos', 7000);
      }
    });

  }

  onSearchChange(newValue: string): void {
    this.searchProducto = newValue;
    this.performProductSearch();
  }

  seleccionarProducto(producto: Producto): void {
    this.formOrdenDetalle.idProducto = producto.id;
    this.searchProducto = `${producto.id} - ${producto.nombre}`;
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

  // Clear filters
  clearFilters(): void {
    this.selectedTipo = '';
    this.selectedSubTipo = '';
    this.selectedMedida = '';
    this.selectedModelo = '';
    this.selectedColor = '';
    this.refreshConfig();   // resets dropdowns + triggers search
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
    // Also reset filters when creating a new line
    this.selectedTipo = '';
    this.selectedSubTipo = '';
    this.selectedMedida = '';
    this.selectedModelo = '';
    this.selectedColor = '';
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
      this.toastService.showToast('error', 'Cantidad inválida', 'La cantidad debe ser mayor a cero', 6000);
      return; // ← detiene el envío
    }

    if (selectedPrecio && selectedPrecio.cantidadRequerida > 0) {
      const cantidadUsuario = this.formOrdenDetalle.cantidad || 0;

      if (cantidadUsuario < selectedPrecio.cantidadRequerida) {
        this.toastService.showToast(
          'error',
          'Cantidad insuficiente',
          `La cantidad debe ser al menos ${selectedPrecio.cantidadRequerida} para el precio seleccionado (${selectedPrecio.precio})`,
          7000
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
          this.toastService.showToast('success', 'Detalle actualizado', 'El detalle de la orden ha sido actualizado exitosamente', 4000);
          this.ordenDetalleSaved.emit();
          // Delay navigation slightly to ensure toast is visible
          setTimeout(() => this.goBack(), 100);
        },
        error: (err) => {
          console.error('Error actualizando detalle', err);
          this.toastService.showToast('error', 'Error al actualizar', 'No se pudo actualizar el detalle de la orden', 7000);
        }
      });

    } else {
      const createPayload = {
        cantidad: this.formOrdenDetalle.cantidad,
        precioUnitario: this.formOrdenDetalle.precioUnitario,
        subtotal: this.formOrdenDetalle.subtotal
      };

      this.ordenDetalleService.create(this.formOrdenDetalle.idOrden!, this.formOrdenDetalle.idProducto!, createPayload).subscribe({
        next: () => {
          this.toastService.showToast('success', 'Detalle creado', 'El detalle de la orden ha sido creado exitosamente', 4000);
          this.ordenDetalleSaved.emit();
          // Delay navigation slightly to ensure toast is visible
          setTimeout(() => this.goBack(), 100);
        },
        error: (err) => {
          console.error('Error creando detalle', err);
          this.toastService.showToast('error', 'Error al crear', 'No se pudo crear el detalle de la orden', 7000);
        }
      });
    }
  }
}