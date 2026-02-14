import { Component, Input, Output, EventEmitter, OnChanges, OnInit, ChangeDetectorRef } from '@angular/core';

import { CommonModule, Location  } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { OrdenDetalleService } from '../orden-detalle.service';
import { OrdenDetalle } from '../orden-detalle.model';


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
  ordenId: number | null = null;

  constructor(
    private ordenDetalleService: OrdenDetalleService,
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

  ngOnInit(): void {
    const params = this.route.snapshot.paramMap;

    const ordenIdStr = params.get('ordenId');
    if (!ordenIdStr) {
      console.error('No se encontró ordenId en la ruta');
      return;
    }
    const ordenId = Number(ordenIdStr);
    this.formOrdenDetalle.idOrden = ordenId;

    const idOrdenDetalleStr = params.get('idOrdenDetalle');
    const idProductoStr = params.get('idProducto');

    if (idOrdenDetalleStr && idProductoStr) {
      // Modo edición
      const linea = Number(idOrdenDetalleStr);
      const prodId = Number(idProductoStr);

      this.ordenDetalleService.getByCompositeKey(ordenId, linea, prodId).subscribe({
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
    this.updateSubtotal(); // Ensure subtotal is correct on load
  }

  goBack(): void {
    this.location.back();
  }

  updateSubtotal(): void {
    const cantidad = this.formOrdenDetalle.cantidad || 0;
    const precio = this.formOrdenDetalle.precioUnitario || 0;
    this.formOrdenDetalle.subtotal = cantidad * precio;
  }

  resetForm(): void {
    // Conservamos el idOrden actual (importante para creación)
    const currentOrdenId = this.formOrdenDetalle.idOrden ?? 0;
    this.formOrdenDetalle = {
      idOrden: currentOrdenId,
      idOrdenDetalle: 0,
      idProducto: 0,
      cantidad: 0,
      precioUnitario: 0,
      subtotal: 0
    };
  }

  onSubmit(): void {
    if (this.isEdit) {
      // Solo enviamos los campos que el backend actualiza
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
      // Payload con objetos anidados (requerido por el backend en create)
      const createPayload = {
        orden: { id: this.formOrdenDetalle.idOrden },
        producto: { id: this.formOrdenDetalle.idProducto },
        cantidad: this.formOrdenDetalle.cantidad,
        precioUnitario: this.formOrdenDetalle.precioUnitario,
        subtotal: this.formOrdenDetalle.subtotal
      };

      this.ordenDetalleService.create(createPayload as any).subscribe({
        next: () => {
          this.ordenDetalleSaved.emit();
          this.goBack();
        },
        error: (err) => console.error('Error creando detalle', err)
      });
    }
  }

}
