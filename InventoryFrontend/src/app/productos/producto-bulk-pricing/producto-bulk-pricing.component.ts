import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { ProductoTipo } from '../../productos-tipos/producto-tipo.model';
import { ProductoTipoService } from '../../productos-tipos/producto-tipo.service';

import { ProductoConfigService } from '../../productos-config/producto-config.service';
import { ToastService } from '../../shared/toast/toast.service';

import { Producto } from '../producto.model';
import {
  ProductoService,
  ProductoBulkPricingOperation,
  ProductoBulkPricingRequest,
  ProductoBulkPricingResponse,
} from '../producto.service';

interface OpRow {
  type: 'ADD_PRECIO' | 'REMOVE_PRECIO' | 'UPSERT_COSTO' | 'REMOVE_COSTO';
  precio: number | null;
  tipoCosto: string;
  costo: number | null;
  descripcion: string;
  cantidadRequerida: number | null;
}

@Component({
  selector: 'app-producto-bulk-pricing',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './producto-bulk-pricing.html',
  styleUrls: ['./producto-bulk-pricing.css'],
})
export class ProductoBulkPricingComponent implements OnInit {
  // ---- filters ----
  searchTerm = '';
  selectedTipo = '';
  selectedSubTipo = '';
  selectedMedida = '';
  selectedModelo = '';
  selectedColor = '';

  tipos: ProductoTipo[] = [];
  subTipos: string[] = [];
  medidas: string[] = [];
  modelos: string[] = [];
  colores: string[] = [];

  // ---- result of search ----
  productos: Producto[] = [];
  selectedIds = new Set<number>();
  loading = false;

  // ---- operations to apply ----
  operations: OpRow[] = [];
  readonly tiposCosto = ['General', 'Compra', 'Reparacion', 'Pegado', 'Fallado'];

  submitting = false;
  result: ProductoBulkPricingResponse | null = null;
  errorMessage = '';

  constructor(
    private productoService: ProductoService,
    private productoTipoService: ProductoTipoService,
    private productoConfigService: ProductoConfigService,
    private toastService: ToastService,
    private location: Location,
    private router: Router,
    private cd: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadTipos();
    this.refreshConfig();
  }

  private loadTipos(): void {
    this.productoTipoService.getAll().subscribe({
      next: (data) => { this.tipos = data || []; this.cd.detectChanges(); },
      error: () => { this.tipos = []; },
    });
  }

  refreshConfig(): void {
    this.productoConfigService
      .getConfig(
        this.selectedTipo || undefined,
        this.selectedSubTipo || undefined,
        this.selectedMedida || undefined,
        this.selectedModelo || undefined,
        this.selectedColor || undefined,
      )
      .subscribe({
        next: (config) => {
          this.subTipos = [...new Set(config.map((c) => c.subTipo))];
          this.medidas = [...new Set(config.map((c) => c.medida))];
          this.modelos = [...new Set(config.map((c) => c.modelo))];
          this.colores = [...new Set(config.map((c) => c.color))];

          // Drop invalid selections
          let anyReset = false;
          if (this.selectedSubTipo && !this.subTipos.includes(this.selectedSubTipo)) {
            this.selectedSubTipo = ''; anyReset = true;
          }
          if (this.selectedMedida && !this.medidas.includes(this.selectedMedida)) {
            this.selectedMedida = ''; anyReset = true;
          }
          if (this.selectedModelo && !this.modelos.includes(this.selectedModelo)) {
            this.selectedModelo = ''; anyReset = true;
          }
          if (this.selectedColor && !this.colores.includes(this.selectedColor)) {
            this.selectedColor = ''; anyReset = true;
          }
          if (anyReset) {
            this.refreshConfig();
          } else {
            this.buscarProductos();
            this.cd.detectChanges();
          }
        },
        error: (err) => console.error('[BulkPricing] config error', err),
      });
  }

  onFilterChange(): void {
    this.refreshConfig();
  }

  onSearchTermChange(): void {
    this.buscarProductos();
  }

  buscarProductos(): void {
    this.loading = true;
    const term = this.searchTerm.trim();
    let id: number | undefined;
    let nombre: string | undefined;
    if (term) {
      const n = Number(term);
      if (!isNaN(n) && n > 0 && Number.isInteger(n)) id = n;
      else nombre = term;
    }

    this.productoService.getAllWithFilters({
      id,
      nombre,
      tipo: this.selectedTipo || undefined,
      subTipo: this.selectedSubTipo || undefined,
      medida: this.selectedMedida || undefined,
      modelo: this.selectedModelo || undefined,
      color: this.selectedColor || undefined,
    }).subscribe({
      next: (data) => {
        this.productos = data || [];
        this.loading = false;
        // Keep any previously selected IDs that are still in the new list
        const present = new Set(this.productos.map((p) => p.id));
        this.selectedIds = new Set([...this.selectedIds].filter((id) => present.has(id)));
        this.cd.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.toastService.showToast(
          'error', 'Error', err?.error?.message || 'No se pudieron cargar los productos.',
        );
        this.cd.detectChanges();
      },
    });
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedTipo = '';
    this.selectedSubTipo = '';
    this.selectedMedida = '';
    this.selectedModelo = '';
    this.selectedColor = '';
    this.refreshConfig();
  }

  resetAll(): void {
    this.searchTerm = '';
    this.selectedTipo = '';
    this.selectedSubTipo = '';
    this.selectedMedida = '';
    this.selectedModelo = '';
    this.selectedColor = '';
    this.productos = [];
    this.selectedIds.clear();
    this.operations = [];
    this.result = null;
    this.errorMessage = '';
    this.submitting = false;
    this.refreshConfig();
    this.toastService.showToast('info', 'Formulario reiniciado', 'Listo para una nueva operación.');
  }

  toggleId(id: number, checked: boolean): void {
    if (checked) this.selectedIds.add(id);
    else this.selectedIds.delete(id);
  }
  isSelected(id: number): boolean { return this.selectedIds.has(id); }
  selectAllVisible(): void { this.productos.forEach((p) => this.selectedIds.add(p.id)); }
  clearSelection(): void { this.selectedIds.clear(); }

  // ---- operations ----
  addOp(type: OpRow['type']): void {
    this.operations.push({
      type,
      precio: null,
      tipoCosto: 'General',
      costo: null,
      descripcion: '',
      cantidadRequerida: 0,
    });
  }
  removeOp(i: number): void { this.operations.splice(i, 1); }

  opLabel(t: OpRow['type']): string {
    switch (t) {
      case 'ADD_PRECIO': return 'Agregar precio';
      case 'REMOVE_PRECIO': return 'Eliminar precio';
      case 'UPSERT_COSTO': return 'Crear/Actualizar costo';
      case 'REMOVE_COSTO': return 'Eliminar costo';
    }
  }
  opColor(t: OpRow['type']): string {
    switch (t) {
      case 'ADD_PRECIO': return 'bg-emerald-100 text-emerald-800 border-emerald-300';
      case 'REMOVE_PRECIO': return 'bg-red-100 text-red-800 border-red-300';
      case 'UPSERT_COSTO': return 'bg-blue-100 text-blue-800 border-blue-300';
      case 'REMOVE_COSTO': return 'bg-amber-100 text-amber-800 border-amber-300';
    }
  }

  get canSubmit(): boolean {
    if (this.submitting) return false;
    if (this.selectedIds.size === 0) return false;
    if (this.operations.length === 0) return false;
    // Every op must have its required fields
    return this.operations.every((op) => this.isOpValid(op));
  }

  isOpValid(op: OpRow): boolean {
    switch (op.type) {
      case 'ADD_PRECIO': return op.precio != null && Number(op.precio) > 0;
      case 'REMOVE_PRECIO': return op.precio != null && Number(op.precio) > 0;
      case 'UPSERT_COSTO':
        return !!op.tipoCosto && op.costo != null && Number(op.costo) >= 0;
      case 'REMOVE_COSTO': return !!op.tipoCosto;
    }
  }

  goBack(): void { this.location.back(); }

  onApply(): void {
    if (!this.canSubmit) return;
    if (!confirm(
      `Se aplicarán ${this.operations.length} operación(es) a ${this.selectedIds.size} producto(s). ¿Continuar?`,
    )) return;

    const payloadOps: ProductoBulkPricingOperation[] = this.operations.map((op) => {
      const base: ProductoBulkPricingOperation = { type: op.type };
      if (op.type === 'ADD_PRECIO' || op.type === 'REMOVE_PRECIO') {
        base.precio = Number(op.precio);
      }
      if (op.type === 'UPSERT_COSTO' || op.type === 'REMOVE_COSTO') {
        base.tipoCosto = op.tipoCosto;
      }
      if (op.type === 'UPSERT_COSTO') {
        base.costo = Number(op.costo);
      }
      if (op.type === 'ADD_PRECIO' || op.type === 'UPSERT_COSTO') {
        base.descripcion = (op.descripcion || '').trim();
        base.cantidadRequerida = Number(op.cantidadRequerida || 0);
      }
      return base;
    });

    const payload: ProductoBulkPricingRequest = {
      productoIds: [...this.selectedIds],
      operations: payloadOps,
    };

    this.submitting = true;
    this.errorMessage = '';
    this.result = null;

    this.productoService.applyBulkPricing(payload).subscribe({
      next: (res) => {
        this.submitting = false;
        this.result = res;
        const total = res.aplicadas + res.omitidas;
        if (res.aplicadas > 0 && res.omitidas === 0) {
          this.toastService.showToast(
            'success', 'Operaciones aplicadas',
            `${res.aplicadas} operación(es) aplicadas correctamente.`,
          );
        } else if (res.aplicadas > 0) {
          this.toastService.showToast(
            'warning', 'Aplicación parcial',
            `Aplicadas ${res.aplicadas} de ${total}. ${res.omitidas} omitidas.`,
          );
        } else {
          this.toastService.showToast(
            'error', 'Sin cambios',
            `Ninguna de las ${total} operaciones se pudo aplicar.`,
          );
        }
        this.cd.detectChanges();
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = err?.error?.message || err?.message || 'Error al aplicar operaciones';
        this.toastService.showToast('error', 'Error', this.errorMessage);
        this.cd.detectChanges();
      },
    });
  }

  goToList(): void {
    this.router.navigate(['/productos']);
  }
}
