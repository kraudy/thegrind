import { Component, Input, Output, EventEmitter, OnChanges, OnInit, ChangeDetectorRef} from '@angular/core';

import { CommonModule, Location  } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Producto } from '../../productos/producto.model';
import { ProductoService } from '../../productos/producto.service';

import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { ProductoCosto } from '../producto-costo.model';
import { ProductoCostoService } from '../producto-costo.service';

import { ToastService } from '../../shared/toast/toast.service';


@Component({
  selector: 'app-producto-costo-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './producto-costo-form.html',
  styleUrls: ['./producto-costo-form.css'],
})
export class ProductoCostoFormComponent implements OnChanges, OnInit {
  @Input() productoCosto: ProductoCosto | null = null;
  @Output() productoCostoSaved = new EventEmitter<void>();

  isEdit = false;
  selectedProducto: Producto | null = null;   // solo para mostrar nombre

  formProductoCosto: Partial<ProductoCosto> = {
    costo: 0.00,
    tipoCosto: '',
    descripcion: '',
    cantidadRequerida: 0,
    productoId: undefined
  };

  constructor(
    private productoCostoService: ProductoCostoService,
    private productoService: ProductoService,
    private location: Location,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    const productoIdParam = this.route.snapshot.paramMap.get('productoId');
    const tipoCostoParam = this.route.snapshot.paramMap.get('tipoCosto');
    const queryProductoId = this.route.snapshot.queryParamMap.get('productoId');

    // === MODO EDICIÓN ===
    if (productoIdParam && tipoCostoParam) {
      const productoId = Number(productoIdParam);
      const tipoCosto = tipoCostoParam;

      if (!isNaN(productoId)) {
        this.productoCostoService.getByComposite(productoId, tipoCosto).subscribe({
          next: (data) => {
            this.formProductoCosto = { ...data };
            this.isEdit = true;
            this.loadProductoForDisplay(productoId);
            this.cd.detectChanges();
          },
          error: () => {
            this.resetForm();
            this.isEdit = false;
            this.cd.detectChanges();
          }
        });
        return;
      }
    }

    // === MODO CREACIÓN (viene de detalle del producto con ?productoId=XXX) ===
    // Reset only once at the beginning, then apply productoId if it exists
    this.resetForm();

    if (queryProductoId) {
      const productoId = Number(queryProductoId);
      if (!isNaN(productoId)) {
        this.formProductoCosto.productoId = productoId;
        this.loadProductoForDisplay(productoId);
      }
    }
  }

  ngOnChanges(): void {
    if (this.productoCosto) {
      this.formProductoCosto = { ...this.productoCosto };
      this.isEdit = true;
      if (this.formProductoCosto.productoId) {
        this.loadProductoForDisplay(this.formProductoCosto.productoId);
      }
    } else {
      this.resetForm();
      this.isEdit = false;
    }
  }

  private loadProductoForDisplay(productoId: number): void {
    this.productoService.getById(productoId).subscribe({
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

  goBack(): void {
    this.location.back();
  }

  resetForm(): void {
    this.formProductoCosto = {
      costo: 0.00,
      tipoCosto: '',
      descripcion: '',
      cantidadRequerida: 0,
      productoId: undefined
    };
    this.selectedProducto = null;
  }

  onSubmit(): void {
    if (!this.formProductoCosto.productoId) {
      this.toastService.showToast('error', 'Error', 'No se ha asociado un producto.', 4000);
      return;
    }

    if (this.isEdit) {
      const originalTipoCosto = this.formProductoCosto.tipoCosto!;
      this.productoCostoService.updateComposite(
        this.formProductoCosto.productoId!,
        originalTipoCosto,
        this.formProductoCosto
      ).subscribe({
        next: () => {
          this.toastService.showToast('success', 'Costo actualizado', 'El costo del producto ha sido actualizado correctamente.', 4000);
          this.productoCostoSaved.emit();
          this.location.back();
        },
        error: (err) => {
          console.error('Error updating costo', err);
          this.toastService.showToast('error', 'Error al actualizar', 'No se pudo actualizar el costo del producto.', 6000);
        }
      });
    } else {
      this.productoCostoService.create(this.formProductoCosto as ProductoCosto).subscribe({
        next: () => {
          this.toastService.showToast('success', 'Costo creado', 'El costo del producto ha sido creado correctamente.', 4000);
          this.productoCostoSaved.emit();
          this.location.back();
        },
        error: (err) => {
          console.error('Error creating costo', err);
          this.toastService.showToast('error', 'Error al crear', 'No se pudo crear el costo del producto.', 6000);
        }
      });
    }
  }
}