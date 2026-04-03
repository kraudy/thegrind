import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { ProductoService } from '../producto.service';
import { Producto } from '../producto.model';
import { ProductoPrecioService } from '../../productos-precios/producto-precio.service';
import { ProductoPrecio } from '../../productos-precios/producto-precio.model';

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

  constructor(
    private route: ActivatedRoute,
    private productoService: ProductoService,
    private precioService: ProductoPrecioService,
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
        this.cd.detectChanges();
      },
      error: (err) => console.error('[ProductoDetail] Error cargando producto', err),
    });
  }

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
}