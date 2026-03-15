import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { OrdenSeguimientoDetalleImpresion } from '../orden-seguimiento-detalle-impresion.model';
import { ProductoTipoEstado } from '../../productos-tipo-estados/producto-tipo-estado.model';

@Component({
  selector: 'app-orden-seguimiento-impresion-detalle-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orden-seguimiento-impresion-detalle-list.html',
  styleUrls: ['./orden-seguimiento-impresion-detalle-list.css'],
})
export class OrdenSeguimientoImpresionDetalleListComponent implements OnInit {
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
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.idOrden = Number(this.route.snapshot.paramMap.get('idOrden'));
    this.clienteNombre = String(this.route.snapshot.paramMap.get('clienteNombre'));
    this.load();
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
            this.ordenSeguimientoService.advance(det.idOrden, det.idOrdenDetalle).subscribe(() => {
              // Reload after advancement
              this.load();
            });
          }
        });
        
        this.cd.detectChanges();
      },
      error: (err) => console.error('❌ Error cargando detalle de impresión', err),
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
    this.ordenSeguimientoService.advance(det.idOrden, det.idOrdenDetalle).subscribe(() => {
      this.load();
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

  goBack() {
    this.router.navigate(['/ordenes-seguimiento-impresion']);
  }

  // Progress methods
  addProgress(det: OrdenSeguimientoDetalleImpresion) {
    const progressAmount = this.progressInputs[det.idOrdenDetalle] || 0;
    if (progressAmount <= 0) {
      alert('Ingrese una cantidad válida para agregar progreso.');
      return;
    }

    this.ordenSeguimientoService.progresoTrabajo(det.idOrden, det.idOrdenDetalle, progressAmount).subscribe({
      next: () => {
        // Reset input
        this.progressInputs[det.idOrdenDetalle] = 0;
        // Reload data
        this.load();
        // Note: Automatic advancement will be checked after load completes
      },
      error: (err) => {
        console.error('Error adding progress:', err);
        alert('Error al agregar progreso. Verifique la cantidad.');
      }
    });
  }

  confirmAdvance(det: OrdenSeguimientoDetalleImpresion) {
    this.advanceDetail(det);
  }

  checkAndAdvance(det: OrdenSeguimientoDetalleImpresion) {
    // If no pending work, automatically advance
    if (det.cantidadPendiente <= 0) {
      this.advanceDetail(det);
    } else {
      // Show advance dialog
      this.showAdvanceDialog[det.idOrdenDetalle] = true;
    }
  }
}