import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { OrdenSeguimientoDetalleImpresion } from '../orden-seguimiento-detalle-impresion.model';
import { ProductoTipoEstado } from '../../productos-tipo-estados/producto-tipo-estado.model';

import { NotificationService } from '../../shared/notification.service';
import { ToastService } from '../../shared/toast/toast.service';  

@Component({
  selector: 'app-orden-seguimiento-impresion-detalle-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orden-seguimiento-impresion-detalle-list.html',
  styleUrls: ['./orden-seguimiento-impresion-detalle-list.css'],
})
export class OrdenSeguimientoImpresionDetalleListComponent implements OnInit, OnDestroy  {

  private destroy$ = new Subject<void>();

  idOrden!: number;
  detalles: OrdenSeguimientoDetalleImpresion[] = [];
  clienteNombre = 'Cargando...';

  possibleStatesMap = new Map<number, ProductoTipoEstado[]>();
  historyMap = new Map<number, any[]>();
  currentStateMap = new Map<number, string>();
  isOrderCompleted = false;

  // Progress tracking
  progressInputs: { [key: number]: number } = {};
  showAdvanceDialog: { [key: number]: boolean } = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ordenSeguimientoService: OrdenSeguimientoService,
    private notificationService: NotificationService,
    private toastService: ToastService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.notificationService.connect();   // still safe (idempotent now)

    // listen only to "seguimiento" refreshes
    this.notificationService.refreshNeeded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(view => {
        if (view === 'seguimiento') {
          console.log('🔄 Real-time refresh triggered for impresión detail');
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
    // Optional: this.notificationService.disconnect();  ← only if this is the last component
  }

  load() {
    this.ordenSeguimientoService.getOrdenDetalleParaImpresion(this.idOrden).subscribe({
      next: (data) => {
        this.detalles = data || [];
        this.loadStepperDataForAll();
        
        // Check for automatic advancement
        this.detalles.forEach(det => {
          if (det.cantidadPendiente <= 0 && det.permiteMover) {
            // Automatically advance if no pending work and movement is allowed
            this.advanceDetail(det);
          }
        });
        
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('❌ Error cargando detalle de impresión', err);
        this.toastService.showToast('error', 'Error al cargar', 'No se pudo cargar el detalle de impresión', 6000);
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

      this.ordenSeguimientoService.getPossibleStates(det.tipoProducto, det.subTipoProducto).subscribe((states) => {
        this.possibleStatesMap.set(detId, states || []);
        this.cd.detectChanges();
      });

      this.ordenSeguimientoService.getByDetalle(det.idOrden, det.idOrdenDetalle).subscribe((hist) => {
        this.historyMap.set(detId, hist || []);
        this.cd.detectChanges();
      });
    });
    
    // If no details are returned, the order is completed (all items moved to next stage)
    if (this.detalles.length === 0) {
      this.isOrderCompleted = true;
    }
  }

  advanceDetail(det: OrdenSeguimientoDetalleImpresion) {
    if (det.estadoActual === 'Normal' || det.estadoActual === 'Reparacion') {
      // Normal advancement
      this.ordenSeguimientoService.advance(det.idOrden, det.idOrdenDetalle).subscribe({
        next: () => {
          this.toastService.showToast('success', 'Éxito', `Detalle #${det.idOrdenDetalle} (${det.nombreProducto || 'Sin nombre'}) - Estado avanzado correctamente`, 4000);
          this.load();
        },
        error: (err) => {
          console.error('Error al avanzar estado:', err);
          this.toastService.showToast('error', 'Error al avanzar', 'No se pudo avanzar el estado del detalle', 7000);
        }
      });
      return;
    }


    // Impresion logic
    if (det.tipoProducto === 'Retablos') {
      this.ordenSeguimientoService.getPegadores().subscribe({
        next: (pegadores) => {
          if (pegadores && pegadores.length > 0) {
            const pegador = pegadores[0]; // Take first pegador
            this.ordenSeguimientoService.assignTrabajo(det.idOrden, det.idOrdenDetalle, pegador.usuario).subscribe({
              next: () => {
                this.toastService.showToast('success', 'Éxito', 'Pegador asignado correctamente', 4000);
                // Assignment successful, now advance
                this.ordenSeguimientoService.advance(det.idOrden, det.idOrdenDetalle).subscribe({
                  next: () => {
                    this.toastService.showToast('success', 'Éxito', `Detalle #${det.idOrdenDetalle} (${det.nombreProducto || 'Sin nombre'}) - Estado avanzado correctamente`, 4000);
                    this.load();
                  },
                  error: (err) => {
                    console.error('Error al avanzar estado:', err);
                    this.toastService.showToast('error', 'Error al avanzar', 'No se pudo avanzar el estado del detalle', 7000);
                  }
                });
              },
              error: (err) => {
                console.error('Error assigning pegador:', err);
                this.toastService.showToast('error', 'Error al asignar pegador', 'No se pudo asignar el pegador', 6000);
              }
            });
          } else {
            this.toastService.showToast('error', 'Sin pegadores', 'No hay pegadores disponibles', 6000);
          }
        },
        error: (err) => {
          console.error('Error getting pegadores:', err);
          this.toastService.showToast('error', 'Error al obtener pegadores', 'No se pudo obtener la lista de pegadores', 6000);
        }
      });
    } else if (det.tipoProducto === 'Molduras') { //TODO: Notar como aqui se asinga primero y despues se avanza
        this.ordenSeguimientoService.assignTrabajo(det.idOrden, det.idOrdenDetalle, "alistador").subscribe({
          next: () => {
            this.toastService.showToast('success', 'Éxito', 'Alistador asignado correctamente', 4000);
            // Assignment successful, now advance
            this.ordenSeguimientoService.advance(det.idOrden, det.idOrdenDetalle).subscribe({
              next: () => {
                this.toastService.showToast('success', 'Éxito', `Detalle #${det.idOrdenDetalle} (${det.nombreProducto || 'Sin nombre'}) - Estado avanzado correctamente`, 4000);
                this.load();
              },
              error: (err) => {
                console.error('Error al avanzar estado:', err);
                this.toastService.showToast('error', 'Error al avanzar', 'No se pudo avanzar el estado del detalle', 7000);
              }
            });
          },
          error: (err) => {
            console.error('Error assigning alistador:', err);
            this.toastService.showToast('error', 'Error al asignar alistador', 'No se pudo asignar el alistador', 6000);
          }
        });
    } else if (det.tipoProducto === 'Ampliaciones') { //TODO: Aqui se avance y despues se asigna
      this.ordenSeguimientoService.advance(det.idOrden, det.idOrdenDetalle).subscribe({
        next: () => {
          this.toastService.showToast('success', 'Éxito', `Detalle #${det.idOrdenDetalle} (${det.nombreProducto || 'Sin nombre'}) - Estado avanzado correctamente`, 4000);
          this.load();
          this.ordenSeguimientoService.assignTrabajo(det.idOrden, det.idOrdenDetalle, "entregador").subscribe({
              next: () => {
                this.toastService.showToast('success', 'Éxito', 'Entregador asignado correctamente', 4000);
                console.log('Ampliacion asignada exitosamente a entregador');
              },
              error: (err) => {
                console.error('Error assigning entregador:', err);
                this.toastService.showToast('error', 'Error al asignar entregador', 'No se pudo asignar el entregador', 6000);
              }
          });
        },
        error: (err) => {
          console.error('Error avanzand estado para ampliaciones:');
          this.toastService.showToast('error', 'Error al avanzar', 'No se pudo avanzar el estado para ampliaciones', 7000);
        }
      });
    } else if (det.tipoProducto === 'Baner' || det.tipoProducto === 'Calado' || 
        det.tipoProducto === 'Camisa' || det.tipoProducto === 'Taza' || det.tipoProducto === 'Llavero') {

      this.ordenSeguimientoService.assignTrabajo(det.idOrden, det.idOrdenDetalle, "alistador").subscribe({
        next: () => {
          this.toastService.showToast('success', 'Éxito', 'Alistador asignado correctamente', 4000);
          // Assignment successful, now advance
          this.ordenSeguimientoService.advance(det.idOrden, det.idOrdenDetalle).subscribe({
            next: () => {
              this.toastService.showToast('success', 'Éxito', `Detalle #${det.idOrdenDetalle} (${det.nombreProducto || 'Sin nombre'}) - Estado avanzado correctamente`, 4000);
              this.load();
            },
            error: (err) => {
              console.error('Error al avanzar estado:', err);
              this.toastService.showToast('error', 'Error al avanzar', 'No se pudo avanzar el estado del detalle', 7000);
            }
          });
        },
        error: (err) => {
          console.error('Error assigning alistador:', err);
          this.toastService.showToast('error', 'Error al asignar alistador', 'No se pudo asignar el alistador', 6000);
        }
      });
    }
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

  goBack() {
    this.router.navigate(['/ordenes-seguimiento-impresion']);
  }

  // Progress methods
  addProgress(det: OrdenSeguimientoDetalleImpresion) {
    const progressAmount = this.progressInputs[det.idOrdenDetalle] || 0;
    if (progressAmount <= 0 || progressAmount > det.cantidad) {
      this.toastService.showToast('error', 'Cantidad inválida', 'Ingrese una cantidad válida para agregar progreso', 6000);
      return;
    }

    this.ordenSeguimientoService.progresoTrabajo(det.idOrden, det.idOrdenDetalle, progressAmount).subscribe({
      next: () => {
        this.toastService.showToast(
          'info',
          'Progreso agregado',
          `Detalle #${det.idOrdenDetalle} (${det.nombreProducto || 'Sin nombre'}) - Se agregaron ${progressAmount} unidades correctamente`,
          5500
        );

        // Reset input
        this.progressInputs[det.idOrdenDetalle] = 0;
        // Reload data
        this.load();
        // Note: Automatic advancement will be checked after load completes
      },
      error: (err) => {
        console.error('Error adding progress:', err);
        this.toastService.showToast('error', 'Error al agregar progreso', 'Verifique la cantidad', 7000);
      }
    });
  }

  confirmAdvance(det: OrdenSeguimientoDetalleImpresion) {
    this.advanceDetail(det);
  }

  checkAndAdvance(det: OrdenSeguimientoDetalleImpresion) {
    // If no pending work, automatically advance
    console.log('Cantidad pendiente: ', det.cantidadPendiente);
    if (det.cantidadPendiente <= 0) {
      this.advanceDetail(det);
    } else {
      // Show advance dialog
      this.showAdvanceDialog[det.idOrdenDetalle] = true;
    }
  }
}