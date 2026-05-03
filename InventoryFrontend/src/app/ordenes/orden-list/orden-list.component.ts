import { Component, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { OrdenService } from '../orden.service';
import { Orden } from '../orden.model';

@Component({
  selector: 'app-orden-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './orden-list.html',
  styleUrls: ['./orden-list.css'],
})
export class OrdenListComponent implements OnInit {
  ordenes: Orden[] = [];

  loading = false;
  errorMessage = '';

  // === Filtros ===
  searchTerm = '';
  selectedEstadoFilter = '';   // '' = Todos

  constructor(
      private ordenService: OrdenService,
      private router: Router,
      private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadOrdenes();
  }

  loadOrdenes(): void {
    this.ordenService.getAllFiltered(this.searchTerm, this.selectedEstadoFilter)
      .subscribe({
        next: (data) => {
          this.ordenes = data || [];
          this.loading = false;
          this.cd.detectChanges();
        },
        error: (err: any) => {
          console.error('[OrdenList] failed to load ordenes', err);
          this.ordenes = [];
          this.loading = false;

          if (err?.status === 0) {
            this.errorMessage = 'No se puede conectar con el backend. ¿Está corriendo el servidor Spring?';
          } else if (err?.status) {
            this.errorMessage = `Error ${err.status}: ${err?.message || err?.statusText || 'desconocido'}`;
          } else {
            this.errorMessage = 'Error inesperado al cargar las órdenes';
          }
          this.cd.detectChanges();
        }
      });
  }

  onFilterChange(): void {
    this.loadOrdenes();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedEstadoFilter = '';
    this.loadOrdenes();
  }

  canModifyOrden(orden?: Orden): boolean {
    return (orden?.estado || '').toLowerCase() === 'recibida';
  }

  deleteOrden(orden?: Orden): void {
    if (!orden?.id || !this.canModifyOrden(orden)) {
      return;
    }

    if (confirm('¿Seguro desea eliminar?')){
      this.ordenService.delete(orden.id).subscribe(() => this.loadOrdenes());
    }
  }

  viewDetails(ordenId: number): void {
    this.router.navigate(['/ordenes-detalle', ordenId], { queryParams: { from: 'list' } });
  }
}
