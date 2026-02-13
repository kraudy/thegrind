import { Component, Input, Output, EventEmitter, OnChanges, OnInit, ChangeDetectorRef} from '@angular/core';

import { CommonModule, Location  } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { ProductoTipo } from '../../productos-tipos/producto-tipo.model';
import { ProductoTipoService } from '../../productos-tipos/producto-tipo.service';

import { ProductoService } from '../producto.service';
import { Producto } from '../producto.model';

@Component({
  selector: 'app-producto-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './producto-form.html',
  styleUrls: ['./producto-form.css'],
})
export class ProductoFormComponent implements OnChanges, OnInit {
  @Input() producto: Producto | null = null;
  @Output() productoSaved = new EventEmitter<void>();
  isEdit = false;
  productoId: number | null = null;
  tipos: ProductoTipo[] = [];

  constructor(
    private productoService: ProductoService,
    private productoTipoService: ProductoTipoService,
    private location: Location,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef
  ) {}

  formProducto: Partial<Producto> = { tipoProducto: '', nombre: '', descripcion: ''};

  ngOnInit(): void {
    if (!this.producto) { // If no id in route — ensure fresh form
      this.resetForm();
      this.isEdit = false;
    }

    // Cargar tipos de producto siempre
    this.productoTipoService.getAll().subscribe({
      next: (data) => {
        this.tipos = data || [];
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[ProductoForm] failed to load tipos de producto', err);
        this.tipos = [];
        this.cd.detectChanges();
      }
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {return;}
    const id = Number(idParam);
    if (isNaN(id)) {return;}

    this.productoId = id;
    this.productoService.getById(id).subscribe({
      next: (data) => {
        this.formProducto = { ...data };
        this.isEdit = true;
        console.log('[ProductoForm] loaded producto for edit', data);
        this.cd.detectChanges();  // Force view update
      },
      error: (err) => {
        console.error('[ProductoForm] failed to load producto', err);
        // Keep form reset so user can create, but mark not edit on failure
        this.resetForm();
        this.isEdit = false;
        this.cd.detectChanges();  // Force view update
      }
    });
  }

  ngOnChanges(): void {
    if (this.producto) {
      this.formProducto = { ...this.producto };
      this.isEdit = !!this.producto.id;  // true if editing an existing producto
      this.cd.detectChanges();
    } else {
      this.resetForm();
      this.isEdit = false;
    }
  }

  goBack(): void {
    this.location.back();
  }

  resetForm(): void {
    this.formProducto = {tipoProducto: '', nombre: '', descripcion: ''};
  }

  onSubmit(): void {
    if (this.formProducto.id) {
      console.log('Updating producto: ' + this.formProducto.id);
      this.productoService.update(this.formProducto.id, this.formProducto).subscribe(() => {
        this.productoSaved.emit();
        this.resetForm();
        this.location.back();
      });
    } else {
      console.log('Creating producto: ');
      this.productoService.create(this.formProducto).subscribe(() => {
        this.productoSaved.emit();
        this.resetForm();
        this.location.back();
      });
    }
  }

}
