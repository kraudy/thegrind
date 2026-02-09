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

  formOrden: Orden = { 
    idCliente: 0,
    recibidaPor: '',
    preparadaPor: '',
    despachadaPor: '',
    totalMonto: 0,
    totalProductos: 0,
    fechaCreacion: null,
    fechaEntrega: null,
    fechaPreparada: null,
    fechaDespachada: null,
    fechaModificacion: null,
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
    this.formOrden = {
      id: data.id,
      idCliente: data.idCliente || 0,
      recibidaPor: data.recibidaPor || '',
      preparadaPor: data.preparadaPor || '',
      despachadaPor: data.despachadaPor || '',
      totalMonto: data.totalMonto || 0,
      totalProductos: data.totalProductos || 0,
      fechaCreacion: data.fechaCreacion || null,
      fechaEntrega: data.fechaEntrega || null,
      fechaPreparada: data.fechaPreparada || null,
      fechaDespachada: data.fechaDespachada || null,
      fechaModificacion: data.fechaModificacion || null,
      estado: data.estado || 'Recibida',
    };
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
      totalProductos: 1,
      fechaCreacion: null,
      fechaEntrega: null,
      fechaPreparada: null,
      fechaDespachada: null,
      fechaModificacion: null,
      estado: 'Recibida',
    };
  }

  onSubmit(): void {
    const ordenToSave: Orden = {
      id: this.formOrden.id,
      idCliente: this.formOrden.idCliente,
      recibidaPor: this.formOrden.recibidaPor,
      preparadaPor: this.formOrden.preparadaPor,
      despachadaPor: this.formOrden.despachadaPor,
      totalMonto: this.formOrden.totalMonto,
      totalProductos: this.formOrden.totalProductos,
      estado: this.formOrden.estado,
      fechaCreacion: this.formOrden.fechaCreacion,
      fechaEntrega: this.formOrden.fechaEntrega,
      fechaPreparada: this.formOrden.fechaPreparada,
      fechaDespachada: this.formOrden.fechaDespachada,
      fechaModificacion: this.formOrden.fechaModificacion,
    };

    // Valores automáticos
    if (!this.isEdit) {
      ordenToSave.fechaCreacion = new Date();
    } else {
      ordenToSave.fechaModificacion = new Date();
    }

    if (this.formOrden.id) {
      console.log('Updating orden:', this.formOrden.id);
      this.ordenService.update(this.formOrden.id, ordenToSave).subscribe(() => {
        this.ordenSaved.emit();
        this.goBack();
      });
    } else {
      console.log('Creating orden');
      this.ordenService.create(ordenToSave).subscribe(() => {
        this.ordenSaved.emit();
        this.resetForm();
        this.goBack();
      });
    }
  }

}
