import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { OrdenSeguimiento } from '../orden-seguimiento.model';
import { NotificationService } from '../../shared/notification.service';

@Component({
  selector: 'app-orden-seguimiento-repartir-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './orden-seguimiento-repartir-list.html',
  styleUrls: ['./orden-seguimiento-repartir-list.css'],
})
export class OrdenSeguimientoRepartirListComponent implements OnInit, OnDestroy {

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

    // Real-time refresh: listen to "seguimiento" notifications
    this.notificationService.refreshNeeded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(view => {
        if (view === 'seguimiento') {
          console.log('🔄 Real-time refresh triggered for Repartir list');
          this.loadOrdenesParaRepartir();
        }
      });

    this.loadOrdenesParaRepartir();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadOrdenesParaRepartir(): void {
    this.loading = true;
    this.errorMessage = '';

    this.ordenSeguimientoService.getOrdenesParaRepartir().subscribe({
      next: (data) => {
        this.ordenes = data || [];
        this.loading = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[OrdenSeguimientoRepartirList] error loading data', err);
        this.ordenes = [];
        this.loading = false;
        this.errorMessage = 'No se pudo cargar la lista de órdenes para repartir.';
        this.cd.detectChanges();
      },
    });
  }

  goToDetalle(orden: OrdenSeguimiento): void {
    this.router.navigate(['/ordenes-seguimiento-repartir', orden.id, orden.clienteNombre]);
  }

  getTiempoRestanteClass(tiempoRestante: string): string {
    const t = (tiempoRestante || '').toLowerCase();
    if (t.includes('-')) return 'text-red-600';
    if (t.includes('00:')) return 'text-amber-600';
    return 'text-emerald-600';
  }
}
