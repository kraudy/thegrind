import { Component, Input, Output, EventEmitter, OnChanges, OnInit, ChangeDetectorRef} from '@angular/core';

import { CommonModule, Location  } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { ProductoTipo } from '../../productos-tipos/producto-tipo.model';
import { ProductoTipoService } from '../../productos-tipos/producto-tipo.service';

import { ProductoSubTipo } from '../../productos-sub-tipos/producto-sub-tipo.model';
import { ProductoSubTipoService } from '../../productos-sub-tipos/producto-sub-tipo.service';

import { ProductoMedida } from '../../productos-medidas/producto-medida.model';
import { ProductoMedidaService } from '../../productos-medidas/producto-medida.service';

import { ProductoModelo } from '../../productos-modelos/producto-modelo.model';
import { ProductoModeloService } from '../../productos-modelos/producto-modelo.service';

import { ProductoColor } from '../../productos-colores/producto-color.model';
import { ProductoColorService } from '../../productos-colores/producto-color.service';

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
  subTipos: ProductoSubTipo[] = [];
  medidas: ProductoMedida[] = [];
  modelos: ProductoModelo[] = [];
  colores: ProductoColor[] = []; 

  constructor(
    private productoService: ProductoService,
    private productoTipoService: ProductoTipoService,
    private productoSubTipoService: ProductoSubTipoService,
    private productoMedidaService: ProductoMedidaService,
    private productoModeloService: ProductoModeloService,
    private productoColorService: ProductoColorService,
    private location: Location,
    private route: ActivatedRoute,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {}

  formProducto: Partial<Producto> = { 
    tipoProducto: '', 
    subTipoProducto: '', 
    medidaProducto: '',
    modeloProducto: '',
    colorProducto: 'Ninguno', 
    nombre: '', 
    descripcion: ''
  };

  ngOnInit(): void {
    if (!this.producto) { // If no id in route — ensure fresh form
      this.resetForm();
      this.isEdit = false;
    }

    // Cargar tipos de producto
    this.loadTipos();

    // Cargar sub-tipos de producto
    this.loadSubTipos();

    // Cargar medidas de producto
    this.loadMedidas();

    // Cargar modelos de producto
    this.loadModelos();

    // Cargar colores de producto
    this.loadColores();

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

  private loadTipos(): void {
    this.productoTipoService.getAll().subscribe({
      next: (data) => {
        this.tipos = data || [];
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[ProductoForm] failed to load tipo producto', err);
        this.tipos = [];
        this.cd.detectChanges();
      }
    });
  }

  private loadSubTipos(): void {
    this.productoSubTipoService.getAll().subscribe({
      next: (data) => {
        this.subTipos = data || [];
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[ProductoForm] failed to load sub-tipos products', err);
        this.tipos = [];
        this.cd.detectChanges();
      }
    });
  }

  private loadMedidas(): void {
    this.productoMedidaService.getAll().subscribe({
      next: (data) => { this.medidas = data || []; this.cd.detectChanges(); },
      error: (err) => { console.error('[ProductoForm] failed to load medidas', err); this.medidas = []; this.cd.detectChanges(); }
    });
  }

  private loadModelos(): void {
    this.productoModeloService.getAll().subscribe({
      next: (data) => { this.modelos = data || []; this.cd.detectChanges(); },
      error: (err) => { console.error('[ProductoForm] failed to load modelos', err); this.modelos = []; this.cd.detectChanges(); }
    });
  }

  private loadColores(): void {
    this.productoColorService.getAll().subscribe({
      next: (data) => { this.colores = data || []; this.cd.detectChanges(); },
      error: (err) => { 
        console.error('[ProductoForm] failed to load colores', err); 
        this.colores = []; 
        this.cd.detectChanges(); 
      }
    });
  }

  goBack(): void {
    this.location.back();
  }

  resetForm(): void {
    this.formProducto = {tipoProducto: '', subTipoProducto: '', medidaProducto: '',
      modeloProducto: '', colorProducto: 'Ninguno', nombre: '', descripcion: ''};
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
      this.productoService.create(this.formProducto).subscribe((createdProducto) => {
        this.productoSaved.emit();
        this.resetForm();
        // Navigate to the newly created product's detail page
        this.router.navigate(['productos-detalle', createdProducto.id]);
      });
    }
  }

}
