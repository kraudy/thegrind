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

import { ProductoConfig } from '../../productos-config/producto-config.model';
import { ProductoConfigService } from '../../productos-config/producto-config.service';

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

  imagenFile: File | null = null;
  previewUrl: string | null = null;


  constructor(
    private productoService: ProductoService,
    private productoTipoService: ProductoTipoService,
    private productoSubTipoService: ProductoSubTipoService,
    private productoMedidaService: ProductoMedidaService,
    private productoModeloService: ProductoModeloService,
    private productoColorService: ProductoColorService,
    private productoConfigService: ProductoConfigService, 
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

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {return;}
    const id = Number(idParam);
    if (isNaN(id)) {return;}

    this.productoId = id;
    this.productoService.getById(id).subscribe({
      next: (data) => {
        this.formProducto = { ...data };
        this.isEdit = true;
        this.refreshConfig();
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
      this.refreshConfig(); 
      this.cd.detectChanges();
    } else {
      this.resetForm();
      this.isEdit = false;
    }
  }

  private loadTipos(): void {
    this.productoTipoService.getAll().subscribe({
      next: (data) => { this.tipos = data || []; this.cd.detectChanges(); },
      error: (err) => { console.error('[ProductoForm] failed to load tipos', err); this.tipos = []; this.cd.detectChanges(); }
    });
  }

  private refreshConfig(): void {
    this.productoConfigService.getConfig(
      this.formProducto.tipoProducto || undefined,
      this.formProducto.subTipoProducto || undefined,
      this.formProducto.medidaProducto || undefined,
      this.formProducto.modeloProducto || undefined,
      this.formProducto.colorProducto || undefined
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
      },
      error: (err) => console.error('[ProductoForm] failed to load config', err)
    });
  }

  onTipoChange(): void {
    this.formProducto.subTipoProducto = '';
    this.formProducto.medidaProducto = '';
    this.formProducto.modeloProducto = '';
    this.formProducto.colorProducto = 'Ninguno';
    this.refreshConfig();
  }

  onSubTipoChange(): void {
    this.formProducto.medidaProducto = '';
    this.formProducto.modeloProducto = '';
    this.formProducto.colorProducto = 'Ninguno';
    this.refreshConfig();
  }

  onMedidaChange(): void {
    this.formProducto.modeloProducto = '';
    this.formProducto.colorProducto = 'Ninguno';
    this.refreshConfig();
  }

  onModeloChange(): void {
    this.formProducto.colorProducto = 'Ninguno';
    this.refreshConfig();
  }

  goBack(): void {
    this.location.back();
  }

  resetForm(): void {
    this.formProducto = {tipoProducto: '', subTipoProducto: '', medidaProducto: '',
      modeloProducto: '', colorProducto: 'Ninguno', nombre: '', descripcion: ''};
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file && (file.type === 'image/jpeg' || file.type === 'image/png')) {
      this.imagenFile = file;
      this.previewUrl = URL.createObjectURL(file);   // ← add this line
    } else {
      alert('Solo PNG o JPG');
      this.imagenFile = null;
      this.previewUrl = null;
    }
  }

  onSubmit(): void {
    const formData = new FormData();

    // Build the JSON payload that Spring expects under the key "producto"
    const productoPayload: any = {
      tipoProducto: this.formProducto.tipoProducto,
      subTipoProducto: this.formProducto.subTipoProducto,
      medidaProducto: this.formProducto.medidaProducto,
      modeloProducto: this.formProducto.modeloProducto,
      colorProducto: this.formProducto.colorProducto,
      nombre: this.formProducto.nombre?.trim(),
      descripcion: this.formProducto.descripcion?.trim(),
      activo: this.formProducto.activo ?? true
    };

    // Add the PRODUCTO as JSON (this is what @RequestPart("producto") expects)
    formData.append(
      'producto',
      new Blob([JSON.stringify(productoPayload)], { type: 'application/json' })
    );

    // Add the image (if user selected one)
    if (this.imagenFile) {
      formData.append('imagen', this.imagenFile);
    }

    if (this.formProducto.id) {
      console.log('Updating producto: ' + this.formProducto.id);
      this.productoService.update(this.formProducto.id, formData).subscribe(() => {
        this.productoSaved.emit();
        this.resetForm();
        this.location.back();
      });
    } else {
      console.log('Creating producto: ');
      this.productoService.create(formData).subscribe((createdProducto) => {
        this.productoSaved.emit();
        this.resetForm();
        // Navigate to the newly created product's detail page
        this.router.navigate(['productos-detalle', createdProducto.id]);
      });
    }
  }

}
