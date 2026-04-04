import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { ProductoService } from '../producto.service';
import { Producto } from '../producto.model';

import { ProductoPrecioService } from '../../productos-precios/producto-precio.service';
import { ProductoPrecio } from '../../productos-precios/producto-precio.model';

import { ProductoCostoService } from '../../productos-costos/producto-costo.service';
import { ProductoCosto } from '../../productos-costos/producto-costo.model'; 

@Component({
  selector: 'app-producto-detalle',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './producto-detalle.html',
  styleUrls: ['./producto-detalle.css'],
})
export class ProductoDetalleComponent implements OnInit {
  producto: Producto | null = null;
  precios: ProductoPrecio[] = [];
  costos: ProductoCosto[] = [];  

  constructor(
    private route: ActivatedRoute,
    private productoService: ProductoService,
    private precioService: ProductoPrecioService,
    private costoService: ProductoCostoService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) return;

    const id = Number(idParam);
    this.productoService.getById(id).subscribe({
      next: (prod) => {
        this.producto = prod;
        this.loadPrecios(id);
        this.loadCostos(id); 
        this.cd.detectChanges();
      },
      error: (err) => console.error('[ProductoDetail] Error cargando producto', err),
    });
  }

  // Precios
  private loadPrecios(id: number): void {
    this.precioService.getPreciosByProducto(id).subscribe({
      next: (data) => {
        this.precios = data || [];
        this.cd.detectChanges();
      },
      error: (err) => console.error('[ProductoDetail] Error cargando precios', err),
    });
  }

  deletePrecio(productoId: number, precio: number): void {
    if (confirm('¿Seguro que deseas eliminar este precio?')) {
      this.precioService.deleteComposite(productoId, precio).subscribe({
        next: () => this.loadPrecios(this.producto!.id!),
        error: (err) => console.error('Error eliminando precio', err),
      });
    }
  }

  // Costos
  private loadCostos(id: number): void {
    this.costoService.getCostosByProducto(id).subscribe({
      next: (data) => {
        this.costos = data || [];
        this.cd.detectChanges();
      },
      error: (err) => console.error('[ProductoDetail] Error cargando costos', err),
    });
  }

  deleteCosto(productoId: number, tipoCosto: string): void {
    if (confirm('¿Seguro que deseas eliminar este costo?')) {
      this.costoService.deleteComposite(productoId, tipoCosto).subscribe({
        next: () => this.loadCostos(this.producto!.id!),
        error: (err) => console.error('Error eliminando costo', err),
      });
    }
  }

}