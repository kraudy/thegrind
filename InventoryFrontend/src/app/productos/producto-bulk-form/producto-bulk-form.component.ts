import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { ProductoTipo } from '../../productos-tipos/producto-tipo.model';
import { ProductoTipoService } from '../../productos-tipos/producto-tipo.service';

import { ProductoConfigService } from '../../productos-config/producto-config.service';
import { ToastService } from '../../shared/toast/toast.service';
import {
  ProductoService,
  ProductoBulkRequest,
  ProductoBulkPrecioItem,
  ProductoBulkCostoItem,
  ProductoBulkResponse,
} from '../producto.service';

@Component({
  selector: 'app-producto-bulk-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './producto-bulk-form.html',
  styleUrls: ['./producto-bulk-form.css'],
})
export class ProductoBulkFormComponent implements OnInit {
  // shared product attributes
  tipoProducto = '';
  subTipoProducto = '';
  modeloProducto = '';

  // multi-selections
  selectedMedidas = new Set<string>();
  selectedColores = new Set<string>();

  // shared name / description
  nombre = '';
  descripcion = '';
  activo = true;

  // dropdown sources (populated from valid producto_config combos)
  tipos: ProductoTipo[] = [];
  subTipos: string[] = [];
  modelos: string[] = [];
  medidas: string[] = [];
  colores: string[] = [];

  // shared precios / costos applied to every generated producto.
  // Empty by default — the user adds the rows they actually want.
  precios: ProductoBulkPrecioItem[] = [];
  costos: ProductoBulkCostoItem[] = [];

  readonly tiposCosto = ['General', 'Compra', 'Reparacion', 'Pegado', 'Fallado'];

  submitting = false;
  result: ProductoBulkResponse | null = null;
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

  // Pull valid combos from backend and derive the unique attribute lists
  refreshConfig(): void {
    this.productoConfigService
      .getConfig(
        this.tipoProducto || undefined,
        this.subTipoProducto || undefined,
        undefined, // medida — we want ALL valid medidas for the chosen tipo/subTipo/modelo
        this.modeloProducto || undefined,
        undefined, // color — we want ALL valid colores too
      )
      .subscribe({
        next: (config) => {
          this.subTipos = [...new Set(config.map((c) => c.subTipo))];
          this.modelos = [...new Set(config.map((c) => c.modelo))];
          this.medidas = [...new Set(config.map((c) => c.medida))];
          this.colores = [...new Set(config.map((c) => c.color))];

          // Drop any single-select values that are no longer valid
          if (this.subTipoProducto && !this.subTipos.includes(this.subTipoProducto)) {
            this.subTipoProducto = '';
          }
          if (this.modeloProducto && !this.modelos.includes(this.modeloProducto)) {
            this.modeloProducto = '';
          }

          // Drop multi-select values that are no longer valid
          this.selectedMedidas = new Set(
            [...this.selectedMedidas].filter((m) => this.medidas.includes(m)),
          );
          this.selectedColores = new Set(
            [...this.selectedColores].filter((c) => this.colores.includes(c)),
          );

          this.cd.detectChanges();
        },
        error: (err) => console.error('[ProductoBulkForm] failed to load config', err),
      });
  }

  onTipoChange(): void {
    this.subTipoProducto = '';
    this.modeloProducto = '';
    this.selectedMedidas.clear();
    this.selectedColores.clear();
    this.refreshConfig();
  }

  onSubTipoChange(): void {
    this.modeloProducto = '';
    this.selectedMedidas.clear();
    this.selectedColores.clear();
    this.refreshConfig();
  }

  onModeloChange(): void {
    this.selectedMedidas.clear();
    this.selectedColores.clear();
    this.refreshConfig();
  }

  toggleMedida(m: string): void {
    if (this.selectedMedidas.has(m)) this.selectedMedidas.delete(m);
    else this.selectedMedidas.add(m);
  }

  toggleColor(c: string): void {
    if (this.selectedColores.has(c)) this.selectedColores.delete(c);
    else this.selectedColores.add(c);
  }

  selectAllMedidas(): void {
    this.medidas.forEach((m) => this.selectedMedidas.add(m));
  }
  clearMedidas(): void { this.selectedMedidas.clear(); }
  selectAllColores(): void {
    this.colores.forEach((c) => this.selectedColores.add(c));
  }
  clearColores(): void { this.selectedColores.clear(); }

  addPrecio(): void {
    this.precios.push({ precio: 0, descripcion: '', cantidadRequerida: 0 });
  }
  removePrecio(i: number): void {
    this.precios.splice(i, 1);
  }

  addCosto(): void {
    this.costos.push({ tipoCosto: 'General', costo: 0, descripcion: '', cantidadRequerida: 0 });
  }
  removeCosto(i: number): void {
    this.costos.splice(i, 1);
  }

  get totalCombinaciones(): number {
    return this.selectedMedidas.size * this.selectedColores.size;
  }

  /** Preview of how the nombre will look after the color suffix is applied. */
  get nombrePreview(): string {
    const base = (this.nombre || '').trim();
    if (!base) return '';
    const colores = [...this.selectedColores];
    const first = colores.find((c) => c && c.toLowerCase() !== 'ninguno');
    if (!first) return base;
    const titled = first.charAt(0).toUpperCase() + first.slice(1).toLowerCase();
    return `${base} ${titled}`;
  }

  get canSubmit(): boolean {
    return (
      !!this.tipoProducto &&
      !!this.subTipoProducto &&
      !!this.modeloProducto &&
      this.selectedMedidas.size > 0 &&
      this.selectedColores.size > 0 &&
      !!this.nombre.trim() &&
      !this.submitting
    );
  }

  goBack(): void { this.location.back(); }

  onSubmit(): void {
    if (!this.canSubmit) return;

    const precios = this.precios
      .filter((p) => p.precio != null && Number(p.precio) > 0)
      .map((p) => ({
        precio: Number(p.precio),
        descripcion: (p.descripcion || '').trim(),
        cantidadRequerida: Number(p.cantidadRequerida || 0),
      }));

    const costos = this.costos
      .filter((c) => c.tipoCosto && c.costo != null && Number(c.costo) >= 0)
      .map((c) => ({
        tipoCosto: c.tipoCosto,
        costo: Number(c.costo),
        descripcion: (c.descripcion || '').trim(),
        cantidadRequerida: Number(c.cantidadRequerida || 0),
      }));

    const payload: ProductoBulkRequest = {
      tipoProducto: this.tipoProducto,
      subTipoProducto: this.subTipoProducto,
      modeloProducto: this.modeloProducto,
      medidas: [...this.selectedMedidas],
      colores: [...this.selectedColores],
      nombre: this.nombre.trim(),
      descripcion: (this.descripcion || '').trim(),
      activo: this.activo,
      precios,
      costos,
    };

    this.submitting = true;
    this.errorMessage = '';
    this.result = null;

    this.productoService.createBulk(payload).subscribe({
      next: (res) => {
        this.submitting = false;
        this.result = res;

        if (res.totalCreated > 0 && res.skipped.length === 0) {
          this.toastService.showToast(
            'success',
            'Productos creados',
            `${res.totalCreated} producto(s) creados correctamente.`,
          );
        } else if (res.totalCreated > 0 && res.skipped.length > 0) {
          this.toastService.showToast(
            'warning',
            'Creación parcial',
            `Creados ${res.totalCreated} de ${res.totalRequested}. ${res.skipped.length} omitidos.`,
          );
        } else {
          this.toastService.showToast(
            'error',
            'No se creó ningún producto',
            `Todas las ${res.totalRequested} combinaciones fueron omitidas.`,
          );
        }

        this.cd.detectChanges();
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = err?.error?.message || err?.message || 'Error al crear productos';
        this.toastService.showToast('error', 'Error', this.errorMessage);
        this.cd.detectChanges();
      },
    });
  }

  goToList(): void {
    this.router.navigate(['/productos']);
  }
}
