import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { OrdenCalendarioService } from '../../ordenes-calendario/orden-calendario.service';
import { OrdenSeguimientoDetalle } from '../orden-seguimiento-detalle.model';
import { ProductoTipoEstado } from '../../productos-tipo-estados/producto-tipo-estado.model';
import { EstadosPorDetalleDTO } from '../estados-por-detalle.model';

import { NotificationService } from '../../shared/notification.service';
import { ToastService } from '../../shared/toast/toast.service';  

@Component({
  selector: 'app-orden-seguimiento-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './orden-seguimiento-list.html',
  styleUrls: ['./orden-seguimiento-list.css'],
})
export class OrdenSeguimientoListComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  idOrden!: number;
  detalles: OrdenSeguimientoDetalle[] = [];
  clienteNombre = 'Cargando...';

  possibleStatesMap = new Map<number, string[]>();
  currentStateMap = new Map<number, string>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ordenSeguimientoService: OrdenSeguimientoService,
    private ordenCalendarioService: OrdenCalendarioService,
    private notificationService: NotificationService,
    private toastService: ToastService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.notificationService.connect(); 

    // Real-time refresh: listen only to "seguimiento" notifications
    this.notificationService.refreshNeeded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(view => {
        if (view === 'seguimiento') {
          console.log('🔄 Real-time refresh triggered for full seguimiento detail');
          this.load();
        }
      });

    this.idOrden = Number(this.route.snapshot.paramMap.get('idOrden'));
    this.clienteNombre = String(this.route.snapshot.paramMap.get('clienteNombre'));
    this.load();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load() {
    this.ordenSeguimientoService.getFullByOrden(this.idOrden).subscribe({
      next: (data) => {
        this.detalles = data;
        this.loadEstadosPorDetalle();
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('❌ Error cargando seguimiento', err);
        this.toastService.showToast('error', 'Error al cargar', 'No se pudo cargar el seguimiento de la orden', 6000);
      }
    });
  }

  private loadEstadosPorDetalle() {
    this.ordenSeguimientoService.getEstadosPorDetalle(this.idOrden).subscribe({
      next: (data) => {
        data.forEach(item => {
          this.possibleStatesMap.set(item.idOrdenDetalle, item.estados);
          this.currentStateMap.set(item.idOrdenDetalle, item.estadoActual);
        });
        this.cd.detectChanges();
      },
      error: (err) => console.error('❌ Error cargando estados por detalle', err)
    });
  }

  reverseDetail(det: OrdenSeguimientoDetalle) {
    this.ordenSeguimientoService.reverse(det.idOrden, det.idOrdenDetalle).subscribe({
      next: () => {
        this.load();
        this.toastService.showToast('warning', 'Estado regresado', `Detalle #${det.idOrdenDetalle} (${det.nombreProducto || 'Sin nombre'}) regresó correctamente`, 4000);
      },
      error: (error) => {
        this.toastService.showToast('error', 'Error al regresar estado', error.error?.message || 'No se pudo regresar el estado', 7000);
      }
    });
  }

  getPossibleStates(detId: number): string[] {
    return this.possibleStatesMap.get(detId) || [];
  }

  isReached(detId: number, estado: string): boolean {
    return this.possibleStatesMap.get(detId)?.some((h: any) => h.estado === estado) || false;
  }

  isCurrent(detId: number, estado: string | undefined): boolean {
    if (!estado) return false;
    return this.currentStateMap.get(detId) === estado;
  }

  isFirstState(detId: number): boolean {
    const states = this.getPossibleStates(detId);
    if (states.length === 0) return true;
    return this.isCurrent(detId, states[0]);
  }

  goBack() {
    this.router.navigate(['/ordenes-calendario']);
  }

  deleteOrderFromCalendar(): void {
    if (confirm(`¿Estás seguro de que quieres eliminar la orden #${this.idOrden} del calendario?`)) {
      this.ordenCalendarioService.delete(this.idOrden).subscribe({
        next: () => {
          this.toastService.showToast('success', 'Orden eliminada', `La orden #${this.idOrden} fue eliminada del calendario`, 4000);
          this.router.navigate(['/ordenes-calendario']);
        },
        error: (err) => {
          console.error('Error eliminando orden del calendario', err);
          if (err.status === 400) {
            this.toastService.showToast('error', 'No se puede eliminar', 'No se puede eliminar la orden del calendario porque algunos detalles ya han avanzado en su proceso de producción', 7000);
          } else {
            this.toastService.showToast('error', 'Error al eliminar', 'Ocurrió un error al eliminar la orden del calendario', 7000);
          }
        }
      });
    }
  }
}