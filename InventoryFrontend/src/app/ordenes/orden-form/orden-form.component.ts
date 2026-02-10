import { Component, Input, Output, EventEmitter, OnChanges, OnInit, ChangeDetectorRef } from '@angular/core';

import { CommonModule, Location  } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { OrdenService } from '../orden.service';
import { Orden } from '../orden.model';

@Component({
  selector: 'app-orden-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orden-form.html',
  styleUrls: ['./orden-form.css'],
})
export class OrdenFormComponent implements OnChanges, OnInit{
  @Input() orden: Orden | null = null;
  @Output() ordenSaved = new EventEmitter<void>();

  isEdit = false;
  ordenId: number | null = null;

  constructor(
    private ordenService: OrdenService,
    private location: Location,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef
  ) {}

  formOrden: Partial<Orden> = { 
    idCliente: 0,
    recibidaPor: '',
    preparadaPor: '',
    despachadaPor: '',
    totalMonto: 0,
    totalProductos: 0,
    fechaEntrega: null,
    fechaPreparada: null,
    fechaDespachada: null,
    estado: 'Recibida',
  };

  ngOnInit(): void {
    if (!this.orden) {
      this.resetForm();
      this.isEdit = false;
    }

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) { return; }

    const id = Number(idParam);
    if (isNaN(id)) { return; }

    this.ordenId = id;
    this.ordenService.getById(id).subscribe({
      next: (data) => {
        this.loadForm(data);
        this.isEdit = true;
        console.log('[OrdenForm] loaded orden for edit', data);
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[OrdenForm] failed to load orden', err);
        this.resetForm();
        this.isEdit = false;
        this.cd.detectChanges();
      }
    });
  }

  ngOnChanges(): void {
    if (this.orden) {
      this.loadForm(this.orden);
      this.isEdit = !!this.orden.id;
    } else {
      this.resetForm();
      this.isEdit = false;
    }
  }

  private loadForm(data: Orden): void {
    this.formOrden = { ...data }; // Spread the full Orden — includes real dates on edit
  }

  goBack(): void {
    this.location.back();
  }

  resetForm(): void {
    this.formOrden = {
      idCliente: 0,
      recibidaPor: '',
      preparadaPor: '',
      despachadaPor: '',
      totalMonto: 0,
      totalProductos: 0,
      fechaEntrega: null,
      fechaPreparada: null,
      fechaDespachada: null,
      estado: 'Recibida',
    };
  }

  onSubmit(): void {
    const payload: Partial<Orden> = {
      idCliente: this.formOrden.idCliente,
      recibidaPor: this.formOrden.recibidaPor ?? '',
      preparadaPor: this.formOrden.preparadaPor ?? '',
      despachadaPor: this.formOrden.despachadaPor ?? '',
      totalMonto: this.formOrden.totalMonto ?? 0,
      totalProductos: this.formOrden.totalProductos ?? 0,
      fechaEntrega: this.formOrden.fechaEntrega,
      fechaPreparada: this.formOrden.fechaPreparada ?? null,
      fechaDespachada: this.formOrden.fechaDespachada ?? null,
      estado: this.formOrden.estado ?? 'Recibida',
      // ← Explicitly omit fechaCreacion, fechaModificacion (and id for now)
    };

    if (this.isEdit && this.formOrden.id) {
      // Update Orden
      (payload).id = this.formOrden.id;

      this.ordenService.update(this.formOrden.id, payload).subscribe({
        next: () => {
          this.ordenSaved.emit();
          this.goBack();
        },
        complete: () => {
          console.log('Orden payload send sucessfully for update:', payload);
        },
        error: (err) => {
          console.error('Failed to update orden', err);
          alert('Error updating orden: ' + (err?.message || 'Unknown error'));
        }
      });
    } else {
      // Create new Orden
      this.ordenService.create(payload).subscribe({
        next: () => {
          this.ordenSaved.emit();
          this.resetForm();
          this.goBack();
        },
        complete: () => {
          console.log('Orden payload send sucessfully for create:', payload);
        },
        error: (err) => {
          console.error('Failed to create orden', err);
          alert('Error creating orden: ' + (err?.message || 'Unknown error'));
        }
      });
    }
  }

}
