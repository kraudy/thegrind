import { Component, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
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
  selectedCanalFilter = '';    // '' = Todos | 'General' | 'Whatsapp'

  // Canal "lock" cuando el listado fue abierto desde una tarjeta específica (ej. Whatsapp).
  // Si lockedCanal != '' el filtro de canal queda fijo y la UI cambia de tema.
  lockedCanal: '' | 'General' | 'Whatsapp' = '';

  constructor(
      private ordenService: OrdenService,
      private router: Router,
      private route: ActivatedRoute,
      private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      const canal = params.get('canal');
      if (canal === 'Whatsapp' || canal === 'General') {
        this.lockedCanal = canal;
        this.selectedCanalFilter = canal;
      } else {
        this.lockedCanal = '';
      }
      this.loadOrdenes();
    });
  }

  get isWhatsappContext(): boolean {
    return this.lockedCanal === 'Whatsapp';
  }

  loadOrdenes(): void {
    this.ordenService.getAllFiltered(this.searchTerm, this.selectedEstadoFilter, this.selectedCanalFilter)
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
    if (!this.lockedCanal) {
      this.selectedCanalFilter = '';
    }
    this.loadOrdenes();
  }

  // Query params para conservar el canal al navegar
  get canalQueryParams(): any {
    return this.lockedCanal ? { canal: this.lockedCanal } : {};
  }

  viewDetails(ordenId: number): void {
    this.router.navigate(['/ordenes-detalle', ordenId], {
      queryParams: { from: 'list', ...this.canalQueryParams }
    });
  }
}
