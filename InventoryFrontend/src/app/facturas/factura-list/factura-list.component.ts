import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { NotificationService } from '../../shared/notification.service';   // ← NEW
import { Factura } from '../factura.model';
import { FacturaService } from '../factura.service';

@Component({
  selector: 'app-factura-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './factura-list.html',
  styleUrls: ['./factura-list.css'],
})
export class FacturaListComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  facturas: Factura[] = [];
  loading = false;
  errorMessage = '';

  // === Filtros ===
  searchTerm = '';
  selectedEstadoFilter = '';   // '' = Todos

  constructor(
    private facturaService: FacturaService,
    private router: Router,
    private notificationService: NotificationService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.notificationService.connect();

    // Real-time refresh
    this.notificationService.refreshNeeded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(view => {
        if (view === 'facturas') {
          console.log('🔄 Real-time refresh triggered for Facturacion list');
          this.loadFacturas();
        }
      });

    this.loadFacturas();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadFacturas(): void {
    this.loading = true; // ← Activamos loading (mejora respecto al código original de órdenes)
    this.facturaService.getAllFiltered(this.searchTerm, this.selectedEstadoFilter)
      .subscribe({
        next: (data) => {
          this.facturas = data || [];
          this.loading = false;
          this.cd.detectChanges();
        },
        error: (err: any) => {
          console.error('[FacturacionList] failed to load facturas', err);
          this.facturas = [];
          this.loading = false;

          if (err?.status === 0) {
            this.errorMessage = 'No se puede conectar con el backend. ¿Está corriendo el servidor Spring?';
          } else if (err?.status) {
            this.errorMessage = `Error ${err.status}: ${err?.message || err?.statusText || 'desconocido'}`;
          } else {
            this.errorMessage = 'Error inesperado al cargar las facturas';
          }
          this.cd.detectChanges();
        }
      });
  }

  onFilterChange(): void {
    this.loadFacturas();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedEstadoFilter = '';
    this.loadFacturas();
  }

  viewDetails(factura: Factura): void {
    this.router.navigate(['/facturas-detalle', factura.id, factura.clienteNombre], {
      queryParams: {
        totalMontoFactura: factura.total,
        idOrden: factura.idOrden,
        estado: factura.estado,
        usuarioCreacion: factura.usuarioCreacion,
        fechaCreacion: factura.fechaCreacion
      }
    });
  }

  goBackToHome(): void {
    this.router.navigate(['/home']);
  }

}
