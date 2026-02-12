import { Component, Input, Output, EventEmitter, OnChanges, OnInit, ChangeDetectorRef} from '@angular/core';

import { CommonModule, Location  } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { ProductoTipoService } from '../producto-tipo.service';
import { ProductoTipo } from '../producto-tipo.model';

@Component({
  selector: 'app-producto-tipo-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './producto-tipo-form.html',
  styleUrls: ['./producto-tipo-form.css'],
})
export class ProductoTipoFormComponent implements OnChanges, OnInit{
  @Input() productoTipo: ProductoTipo | null = null;
  @Output() productoTipoSaved = new EventEmitter<void>();
  isEdit = false;
  tipo: string | null = null;

  constructor(
    private productoTipoService: ProductoTipoService,
    private location: Location,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef
  ) {}

  formProductoTipo: Partial<ProductoTipo> = { tipo: '', descripcion: ''};

  ngOnInit(): void {
    if (!this.productoTipo) { // If no id in route — ensure fresh form
      this.resetForm();
      this.isEdit = false;
    }
    console.log('[ProductoTipoForm] loading productoTipo for edit, route params:', this.route.snapshot.paramMap);
    const tipoParam = this.route.snapshot.paramMap.get('tipo');
    if (!tipoParam) {return;}
    const tipo = String(tipoParam);
    if (!tipo) {return;}
    this.tipo = tipo;
    this.productoTipoService.getById(tipo).subscribe({
      next: (data) => {
        this.formProductoTipo = { ...data };
        this.isEdit = true;
        console.log('[ProductoTipoForm] loaded producto for edit', data);
        this.cd.detectChanges();  // Force view update
      },
      error: (err) => {
        console.error('[ProductoTipoForm] failed to load producto', err);
        // Keep form reset so user can create, but mark not edit on failure
        this.resetForm();
        this.isEdit = false;
        this.cd.detectChanges();  // Force view update
      }
    });
  }

  ngOnChanges(): void {
    if (this.productoTipo) {
      this.formProductoTipo = { ...this.productoTipo };
      this.isEdit = !!this.productoTipo.tipo;  // true if editing an existing producto
    } else {
      this.resetForm();
      this.isEdit = false;
    }
  }

  goBack(): void {
    this.location.back();
  }

  resetForm(): void {
    this.formProductoTipo = {tipo: '', descripcion: ''};
  }

  onSubmit(): void {
    if (this.formProductoTipo.tipo) {
      console.log('Updating producto tipo: ' + this.formProductoTipo.tipo);
      this.productoTipoService.update(this.formProductoTipo.tipo, this.formProductoTipo).subscribe(() => {
        this.productoTipoSaved.emit();
        this.resetForm();
        this.location.back();
      });
    } else {
      console.log('Creating producto tipo: ');
      this.productoTipoService.create(this.formProductoTipo).subscribe(() => {
        this.productoTipoSaved.emit();
        this.resetForm();
        this.location.back();
      });
    }
  }
}
