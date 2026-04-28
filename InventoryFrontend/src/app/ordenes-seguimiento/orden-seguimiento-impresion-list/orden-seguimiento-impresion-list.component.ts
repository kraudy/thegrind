import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { OrdenSeguimiento } from '../orden-seguimiento.model';

import { NotificationService } from '../../shared/notification.service';

@Component({
  selector: 'app-orden-seguimiento-impresion-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './orden-seguimiento-impresion-list.html',
  styleUrls: ['./orden-seguimiento-impresion-list.css'],
})
export class OrdenSeguimientoImpresionListComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  ordenes: OrdenSeguimiento[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private ordenSeguimientoService: OrdenSeguimientoService,
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
        if (view === 'seguimiento') {
          console.log('🔄 Real-time refresh triggered for Impresión list');
          this.loadOrdenesParaImpresion();
        }
      });

    this.loadOrdenesParaImpresion();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadOrdenesParaImpresion(): void {
    this.loading = true;
    this.errorMessage = '';

    this.ordenSeguimientoService.getOrdenesParaImpresion().subscribe({
      next: (data) => {
        this.ordenes = data || [];
        this.loading = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[OrdenSeguimientoImpresionList] error loading data', err);
        this.ordenes = [];
        this.loading = false;
        this.errorMessage = 'No se pudo cargar la lista de órdenes para impresión.';
        this.cd.detectChanges();
      },
    });
  }

  goToDetalle(orden: OrdenSeguimiento): void {
    // Route to future detail component (to be implemented later)
    // You can adjust this path when you add that component route.
    this.router.navigate(['/ordenes-seguimiento-impresion', orden.id, orden.clienteNombre]);
  }

  getTiempoRestanteClass(tiempoRestante: string): string {
    const t = (tiempoRestante || '').toLowerCase();
    if (t.includes('-')) return 'text-red-600';
    if (t.includes('00:')) return 'text-amber-600';
    return 'text-emerald-600';
  }
}