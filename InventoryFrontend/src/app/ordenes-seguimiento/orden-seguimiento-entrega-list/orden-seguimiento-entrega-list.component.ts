import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { OrdenEntregaFacturacion } from '../orden-entrega-facturacion.model';
import { NotificationService } from '../../shared/notification.service';
import { getPrioridadRowClass } from '../../shared/prioridad.util';

@Component({
  selector: 'app-orden-seguimiento-entrega-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './orden-seguimiento-entrega-list.html',
  styleUrls: ['./orden-seguimiento-entrega-list.css'],
})
export class OrdenSeguimientoEntregaListComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  ordenes: OrdenEntregaFacturacion[] = [];
  loading = false;
  errorMessage = '';

  filterId?: number | null;
  filterCliente = '';
  filterTrabajador = '';

  getPrioridadRowClass = getPrioridadRowClass;

  constructor(
    private ordenSeguimientoService: OrdenSeguimientoService,
    private router: Router,
    private notificationService: NotificationService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.notificationService.connect();

    this.notificationService.refreshNeeded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(view => {
        if (view === 'seguimiento' || view === 'pago') {
          console.log('🔄 Real-time refresh triggered for Entrega/Facturacion list');
          this.loadOrdenes();
        }
      });

    this.loadOrdenes();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadOrdenes(): void {
    this.loading = true;
    this.errorMessage = '';

    this.ordenSeguimientoService.getOrdenesParaEntrega({
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
        console.error('Error loading ordenes para entrega/facturacion:', error);
        this.ordenes = [];
        this.loading = false;
        this.errorMessage = 'No se pudo cargar la lista de ordenes.';
        this.cd.detectChanges();
      }
    });
  }

  onFilterChange(): void {
    if (this.filterId === null || Number.isNaN(this.filterId)) {
      this.filterId = undefined;
    }
    this.loadOrdenes();
  }

  clearFilters(): void {
    this.filterId = undefined;
    this.filterCliente = '';
    this.filterTrabajador = '';
    this.loadOrdenes();
  }

  viewDetails(orden: OrdenEntregaFacturacion): void {
    this.router.navigate(['/ordenes-seguimiento-entrega', orden.id, orden.clienteNombre], {
      queryParams: {
        estado: orden.estado,
        totalProductosOrden: orden.totalProductosOrden,
        totalMontoOrden: orden.totalMontoOrden,
        totalProductosFactura: orden.totalProductosFactura,
        totalMontoFactura: orden.totalMontoFactura,
        saldoPendiente: orden.saldoPendiente
      }
    });
  }

  getTiempoRestanteClass(tiempoRestante: string): string {
    const t = (tiempoRestante || '').toLowerCase();
    if (t.includes('-')) return 'text-red-600';
    if (t.includes('00:')) return 'text-amber-600';
    return 'text-emerald-600';
  }

  getEstadoBadgeClass(estado: string): string {
    return estado === 'Entregado'
      ? 'bg-emerald-100 text-emerald-700'
      : 'bg-amber-100 text-amber-700';
  }
}
