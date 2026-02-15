import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';

import { OrdenDetalleService } from '../orden-detalle.service';
import { OrdenDetalle } from '../orden-detalle.model';

import { Orden } from '../../ordenes/orden.model';
import { OrdenService } from '../../ordenes/orden.service';

@Component({
  selector: 'app-orden-detalle-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './orden-detalle-list.html',
  styleUrls: ['./orden-detalle-list.css'],
})
export class OrdenDetalleListComponent implements OnInit, OnChanges {
  @Input() orden?: Orden;
  ordenDetalles: OrdenDetalle[] = [];

  constructor(
      private ordenDetalleService: OrdenDetalleService,
      private ordenService: OrdenService,      // ← Nuevo
      private route: ActivatedRoute,
      private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (!this.orden) {
      this.loadOrdenFromRoute();
    } else {
      this.loadOrdenesDetalles();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Soporte para cuando se pasa [orden] como input (ej: en una vista padre)
    if (changes['orden'] && this.orden?.id) {
      this.loadOrdenesDetalles();
    }
  }

  private loadOrdenFromRoute(): void {
    this.route.paramMap.subscribe(params => {
      const ordenIdStr = params.get('ordenId');
      if (!ordenIdStr) {
        console.log('[OrdenDetalleList] No ordenId in route params');
        this.orden = undefined;
        this.ordenDetalles = [];
        return;
      }

      const ordenId = Number(ordenIdStr);
      console.log('[OrdenDetalleList] Loading orden with id from route:', ordenId);

      this.ordenService.getById(ordenId).subscribe({
        next: (orden) => {
          this.orden = orden;
          this.loadOrdenesDetalles();
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error('[OrdenDetalleList] Error cargando orden', err);
          this.orden = undefined;
          this.ordenDetalles = [];
        }
      });
    });
  }

  loadOrdenesDetalles(): void {
    if (!this.orden?.id) {
      this.ordenDetalles = [];
      return;
    }
    // It must receive the order as input and load only the details for that order
    this.ordenDetalleService.getByOrden(this.orden.id).subscribe({
      next: (data) => {
        this.ordenDetalles = data || [];
        console.log('[OrdenDetalleList] loaded ordenes detalles:', this.ordenDetalles.length, this.ordenDetalles);
        this.cd.detectChanges(); // force view update if zone isn't triggering it
      },
      error: (err) => {
        // log full error to inspect status/code in the console
        console.error('[OrdenDetalleList] failed to load ordenes detalles', err);
        this.ordenDetalles = [];
        if (err?.status === 0) {
          console.log('Cannot reach backend (network error). Is the Spring server running?');
        } else if (err?.status) {
          console.log(`Backend error ${err.status}: ${err?.message || err?.statusText || 'unknown'}`);
        } else {
          console.log('Unexpected error loading ordenes (check console)');
        }
      }
    });
  }

  deleteOrdenDetalle(idOrden: number, idOrdenDetalle: number, idProducto: number): void {
    if (confirm('¿Seguro desea eliminar este detalle de orden?')) {
      this.ordenDetalleService.delete(idOrden, idOrdenDetalle, idProducto).subscribe({
        next: () => {
          console.log('Detalle eliminado correctamente');
          this.loadOrdenesDetalles();
        },
        error: (err) => {
          console.error('Error al eliminar detalle', err);
          alert('No se pudo eliminar el detalle. Ver consola para más detalles.');
        }
      });
    }
  }
}
