import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { OrdenSeguimiento } from '../orden-seguimiento.model';
import { NotificationService } from '../../shared/notification.service';   // ← NEW

@Component({
  selector: 'app-orden-seguimiento-entrega-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './orden-seguimiento-entrega-list.html',
  styleUrls: ['./orden-seguimiento-entrega-list.css'],
})
export class OrdenSeguimientoEntregaListComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  ordenes: OrdenSeguimiento[] = [];
  loading = false;
  errorMessage = '';

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
          this.loadOrdenesParaEntrega();
        }
      });

    this.loadOrdenesParaEntrega();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadOrdenesParaEntrega(): void {
    this.loading = true;
    this.errorMessage = '';

    this.ordenSeguimientoService.getOrdenesParaEntrega().subscribe({
      next: (ordenes) => {
        this.ordenes = ordenes || [];
        this.loading = false;
        this.cd.detectChanges();
      },
      error: (error) => {
        console.error('Error loading ordenes para entrega:', error);
        this.ordenes = [];
        this.loading = false;
        this.errorMessage = 'No se pudo cargar la lista de órdenes para entrega.';
        this.cd.detectChanges();
      }
    });
  }

  viewDetails(orden: OrdenSeguimiento): void {
    this.router.navigate(['/ordenes-seguimiento-entrega', orden.id, orden.clienteNombre]);
  }

  getTiempoRestanteClass(tiempoRestante: string): string {
    const t = (tiempoRestante || '').toLowerCase();
    if (t.includes('-')) return 'text-red-600';
    if (t.includes('00:')) return 'text-amber-600';
    return 'text-emerald-600';
  }
}
