import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router'; 
import { FormsModule } from '@angular/forms';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { OrdenSeguimientoDetalleGeneral } from '../orden-seguimiento-detalle-general.model';
import { NotificationService } from '../../shared/notification.service';

@Component({
  selector: 'app-orden-seguimiento-general-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './orden-seguimiento-general-list.html',
  styleUrls: ['./orden-seguimiento-general-list.css'],
})
export class OrdenSeguimientoGeneralListComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  ordenes: OrdenSeguimientoDetalleGeneral[] = [];
  loading = false;
  errorMessage = '';

  searchTerm = '';
  selectedStateFilter = ''; // '' = all, or 'Recibida' / 'Repartida' / 'Listo'

  constructor(
    private ordenSeguimientoService: OrdenSeguimientoService,
    private notificationService: NotificationService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.notificationService.connect();

    this.notificationService.refreshNeeded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(view => {
        if (view === 'seguimiento') this.loadOrdenesGeneral();
      });

    this.loadOrdenesGeneral();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadOrdenesGeneral(): void {
    this.loading = true;
    this.errorMessage = '';

    this.ordenSeguimientoService.getOrdenesSeguimientoGeneral(this.searchTerm, this.selectedStateFilter)
      .subscribe({
        next: (data) => {
          this.ordenes = data || [];
          this.loading = false;
          this.cd.detectChanges();
        },
        error: (err: any) => {
          console.error('[OrdenSeguimientoGeneralList] error', err);
          this.ordenes = [];
          this.loading = false;
          this.errorMessage = 'No se pudo cargar el seguimiento general.';
          this.cd.detectChanges();
        }
      });
  }

  // Called when user types or changes filter
  onFilterChange() {
    this.loadOrdenesGeneral();
  }

  clearFilters() {
    this.searchTerm = '';
    this.selectedStateFilter = '';
    this.loadOrdenesGeneral();
  }

  getTiempoRestanteClass(tiempoRestante: string): string {
    const t = (tiempoRestante || '').toLowerCase();
    if (t.includes('-')) return 'text-red-600';
    if (t.includes('00:')) return 'text-amber-600';
    return 'text-emerald-600';
  }

  getActiveStates(orden: OrdenSeguimientoDetalleGeneral): { label: string; count: number; color: string }[] {
    const states: any[] = [];

    if (orden.countRepartidas > 0)   states.push({ label: 'Repartida',   count: orden.countRepartidas,   color: 'bg-blue-100 text-blue-700' });
    if (orden.countNormales > 0)     states.push({ label: 'Normal',      count: orden.countNormales,     color: 'bg-indigo-100 text-indigo-700' });
    if (orden.countReparacion > 0)   states.push({ label: 'Reparación',  count: orden.countReparacion,   color: 'bg-orange-100 text-orange-700' });
    if (orden.countImpresion > 0)    states.push({ label: 'Impresión',   count: orden.countImpresion,    color: 'bg-purple-100 text-purple-700' });
    if (orden.countEnmarcado > 0)    states.push({ label: 'Enmarcado',   count: orden.countEnmarcado,    color: 'bg-amber-100 text-amber-700' });
    if (orden.countPegado > 0)       states.push({ label: 'Pegado',      count: orden.countPegado,       color: 'bg-teal-100 text-teal-700' });
    if (orden.countListo > 0)        states.push({ label: 'Listo',       count: orden.countListo,        color: 'bg-green-100 text-green-700' });
    if (orden.countEntregado > 0)    states.push({ label: 'Entregado',   count: orden.countEntregado,    color: 'bg-emerald-100 text-emerald-700' });

    return states;
  }
}