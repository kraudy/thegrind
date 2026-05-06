import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { OrdenSeguimientoService } from '../../ordenes-seguimiento/orden-seguimiento.service';

import { NotificationService } from '../../shared/notification.service';   // ← NEW
import { OrdenFacturacion } from '../orden-facturacion.model';

@Component({
  selector: 'app-orden-facturacion-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './orden-facturacion-list.html',
  styleUrls: ['./orden-facturacion-list.css'],
})
export class OrdenFacturacionListComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  ordenes: OrdenFacturacion[] = [];
  loading = false;
  errorMessage = '';

  filterId?: number | null;
  filterCliente = '';
  filterTrabajador = '';

  constructor(
    private ordenSeguimientoService: OrdenSeguimientoService,
    private router: Router,
    private notificationService: NotificationService,   // ← NEW
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.notificationService.connect();

    // Real-time refresh
    this.notificationService.refreshNeeded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(view => {
        if (view === 'seguimiento') {
          console.log('🔄 Real-time refresh triggered for Entrega list');
          this.loadOrdenesParaFacturacion();
        }
      });

    this.loadOrdenesParaFacturacion();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadOrdenesParaFacturacion(): void {
    this.loading = true;
    this.errorMessage = '';

    this.ordenSeguimientoService.getOrdenesParaFacturacion({
      id: this.filterId ?? undefined,
      cliente: this.filterCliente,
      trabajador: this.filterTrabajador
    }).subscribe({
      next: (ordenes) => {
        this.ordenes = ordenes || [];
        this.loading = false;
        this.cd.detectChanges();
      },
      error: (error) => {
        console.error('Error loading ordenes para facturacion:', error);
        this.ordenes = [];
        this.loading = false;
        this.errorMessage = 'No se pudo cargar la lista de órdenes para facturacion.';
        this.cd.detectChanges();
      }
    });
  }

  onFilterChange(): void {
    if (this.filterId === null || Number.isNaN(this.filterId)) {
      this.filterId = undefined;
    }

    this.loadOrdenesParaFacturacion();
  }

  clearFilters(): void {
    this.filterId = undefined;
    this.filterCliente = '';
    this.filterTrabajador = '';
    this.loadOrdenesParaFacturacion();
  }

  viewDetails(orden: OrdenFacturacion): void {
    this.router.navigate(['/ordenes-facturacion', orden.id, orden.clienteNombre], {
      queryParams: {
        totalProductosOrden: orden.totalProductosOrden,
        totalMontoOrden: orden.totalMontoOrden,
        totalProductosFactura: orden.totalProductosFactura,
        totalMontoFactura: orden.totalMontoFactura,
        saldoPendiente: orden.saldoPendiente   // ← NUEVO
      }
    });
  }

  getTiempoRestanteClass(tiempoRestante: string): string {
    const t = (tiempoRestante || '').toLowerCase();
    if (t.includes('-')) return 'text-red-600';
    if (t.includes('00:')) return 'text-amber-600';
    return 'text-emerald-600';
  }

  goBackToHome(): void {
    this.router.navigate(['/home']);
  }
}
