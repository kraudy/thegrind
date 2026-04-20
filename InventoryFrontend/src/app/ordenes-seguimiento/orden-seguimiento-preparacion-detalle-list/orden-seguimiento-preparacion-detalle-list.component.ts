import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';



import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { ProductoTipoEstado } from '../../productos-tipo-estados/producto-tipo-estado.model';
import { OrdenSeguimientoDetallePreparacion } from '../orden-seguimiento-detalle-preparacion.model';

import { NotificationService } from '../../shared/notification.service';
import { ToastService } from '../../shared/toast/toast.service'; 

@Component({
  selector: 'app-orden-seguimiento-preparacion-detalle-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './orden-seguimiento-preparacion-detalle-list.html',
  styleUrls: ['./orden-seguimiento-preparacion-detalle-list.css'],
})
export class OrdenSeguimientoPreparacionDetalleListComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  idOrden!: number;
  detalles: OrdenSeguimientoDetallePreparacion[] = [];
  clienteNombre = 'Cargando...';

  possibleStatesMap = new Map<number, ProductoTipoEstado[]>();
  historyMap = new Map<number, any[]>();
  currentStateMap = new Map<number, string>();
  isOrderCompleted = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: OrdenSeguimientoService,
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
          console.log('🔄 Real-time refresh triggered for preparación detalle');
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
    this.service.getOrdenDetalleParaPreparacion(this.idOrden).subscribe({
      next: (data) => {
        this.detalles = data || [];
        this.loadStepperDataForAll();
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('❌ Error cargando detalle de preparación', err);
        this.toastService.showToast('error', 'Error al cargar', 'No se pudo cargar la información de la orden', 6000);
      },
    });
  }

  private loadStepperDataForAll() {
    this.possibleStatesMap.clear();
    this.historyMap.clear();
    this.currentStateMap.clear();

    this.detalles.forEach((det) => {
      const detId = det.idOrdenDetalle;
      this.currentStateMap.set(detId, det.estadoActual || '');

      this.service.getPossibleStates(det.tipoProducto, det.subTipoProducto).subscribe((states) => {
        this.possibleStatesMap.set(detId, states || []);
        this.cd.detectChanges();
      });

      this.service.getByDetalle(det.idOrden, det.idOrdenDetalle).subscribe((hist) => {
        this.historyMap.set(detId, hist || []);
        this.cd.detectChanges();
      });
    });

    // If no details are returned, the order is completed (all items moved to next stage)
    if (this.detalles.length === 0 && !this.isOrderCompleted) {
      this.isOrderCompleted = true;
    }
  }

  advanceDetail(det: OrdenSeguimientoDetallePreparacion) {
    // Determine the next state before advancing
    const possibleStates = this.getPossibleStates(det.idOrdenDetalle);
    const currentIndex = possibleStates.findIndex(s => s.estado === det.estadoActual);
    const nextState = possibleStates[currentIndex + 1]?.estado || 'Estado final';

    //TODO: Componer esto para que quede dinamico como en impresion
    if (det.estadoActual === 'Pegado' || det.estadoActual === 'Enmarcado' || 
        det.estadoActual === 'Armado' || det.estadoActual === 'Calado' || 
        det.estadoActual === 'Sublimacion' || det.estadoActual === 'Bodega') {
      // Add progress with cantidadAsignadaActual before advancing
      this.service.progresoTrabajo(det.idOrden, det.idOrdenDetalle, det.cantidadAsignadaActual).subscribe({
        next: () => {
          console.log('Progress ' + det.cantidadAsignadaActual + ' added successfully for Pegado/Enmarcado state');
        },
        error: (err) => {
          console.error('Error adding progress for Pegado/Enmarcado state:', err);
          this.toastService.showToast(
            'error',
            'Error al registrar progreso',
            'No se pudo registrar el progreso del trabajo. Inténtalo nuevamente.',
            7000
          );
          return; // Stop further execution to prevent advancing if progress update fails
        }
      });
    }

    //TODO: Change usuario for logging later
    this.service.advance(det.idOrden, det.idOrdenDetalle).subscribe({
      next: () => {
        this.load();
        this.service.assignTrabajo(det.idOrden, det.idOrdenDetalle, "entregador").subscribe({
          next: () => {
            console.log('Trabajo ' + det.idOrden + ' ' + det.idOrdenDetalle + ' ' + det.tipoProducto + ' ' + det.subTipoProducto + ' asignado exitosamente al entregador');

            this.toastService.showToast(
              'success',
              'Estado avanzado',
              `Detalle #${det.idOrdenDetalle} (${det.nombreProducto || 'Sin nombre'}) avanzó a ${nextState} correctamente y se asignó al entregador`,
              4000
            );

          },
          //TODO: Deberia resetear el progreso si hay error al asignar?
          error: (err) => {
            console.error('Error assigning entregador:', err);
            this.toastService.showToast(
              'error',
              'Error al asignar entregador',
              'No se pudo asignar el entregador. Inténtalo nuevamente.',
              6000
            );
          }
        });
      },
      error: (err) => {
        console.error('Error avanzando estado:', err);
        this.toastService.showToast(
          'error',
          'Error al avanzar estado',
          'No se pudo avanzar el estado del detalle.',
          7000
        );
      }
    });
  }

  getPossibleStates(detId: number): ProductoTipoEstado[] {
    return this.possibleStatesMap.get(detId) || [];
  }

  isReached(detId: number, estado: string): boolean {
    if (!estado) return false;
    return this.historyMap.get(detId)?.some((h: any) => h.estado === estado) || false;
  }

  isCurrent(detId: number, estado: string | undefined): boolean {
    if (!estado) return false;
    return this.currentStateMap.get(detId) === estado;
  }

  isLastState(detId: number): boolean {
    const states = this.getPossibleStates(detId);
    if (states.length === 0) return true;
    return this.isCurrent(detId, states[states.length - 1]?.estado);
  }

  goBack() {
    this.router.navigate(['/ordenes-seguimiento-preparacion']);
  }
}