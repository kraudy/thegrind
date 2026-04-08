import { Component, Input, OnInit, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms'; 

import { OrdenDetalleService } from '../orden-detalle.service';
import { OrdenDetalle } from '../orden-detalle.model';

import { Orden } from '../../ordenes/orden.model';
import { OrdenService } from '../../ordenes/orden.service';

import { OrdenPagoService } from '../../ordenes-pago/orden-pago.service'; 
import { OrdenPago } from '../../ordenes-pago/orden-pago.model';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NotificationService } from '../../shared/notification.service';

@Component({
  selector: 'app-orden-detalle-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './orden-detalle-list.html',
  styleUrls: ['./orden-detalle-list.css'],
})
export class OrdenDetalleListComponent implements OnInit, OnChanges, OnDestroy {
  @Input() orden?: Orden;
  ordenDetalles: OrdenDetalle[] = [];

  // Adelantos / Pagos
  pagos: OrdenPago[] = [];                    // will hold OrdenPago data
  showRegistrarAdelantoModal = false;

  // Form for new payment
  nuevoPago = {
    monto: 0,
    metodoPago: 'Efectivo',
    codigoReferencia: '',
    banco: '',
    notas: '',
    tipoPago: 'Adelanto'  // default to Adelanto, but could be selectable in the future
  };

  // Real-time WebSocket support
  private destroy$ = new Subject<void>();

  constructor(
      private ordenDetalleService: OrdenDetalleService,
      private ordenPagoService: OrdenPagoService,
      private notificationService: NotificationService,
      private ordenService: OrdenService,
      private route: ActivatedRoute,
      private router: Router,
      private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.notificationService.connect();

    this.notificationService.refreshNeeded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(view => {
        if (view === 'pago') {           // ← triggered when any pago is approved/rejected/created elsewhere
          console.log('🔄 Refresh triggered by WebSocket (pagos) in OrdenDetalleListComponent');
          if (this.orden?.id) {
            this.loadPagos();
          }
        }
      });

    if (!this.orden) {
      this.loadOrdenFromRoute();
    } else {
      this.loadOrdenesDetalles();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['orden'] && this.orden?.id) {
      this.loadOrdenesDetalles();
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadOrdenFromRoute(): void {
    this.route.paramMap.subscribe(params => {
      const ordenIdStr = params.get('ordenId');
      if (!ordenIdStr) {
        console.log('[OrdenDetalleList] No ordenId in route params');
        this.orden = undefined;
        this.ordenDetalles = [];
        return;
      }

      const ordenId = Number(ordenIdStr);
      console.log('[OrdenDetalleList] Loading orden with id from route:', ordenId);

      this.ordenService.getById(ordenId).subscribe({
        next: (orden) => {
          this.orden = orden;
          this.loadOrdenesDetalles();
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error('[OrdenDetalleList] Error cargando orden', err);
          this.orden = undefined;
          this.ordenDetalles = [];
        }
      });
    });
  }

  loadOrdenesDetalles(): void {
    if (!this.orden?.id) {
      this.ordenDetalles = [];
      this.pagos = [];
      return;
    }

    // Load order details
    this.ordenDetalleService.getByOrden(this.orden.id).subscribe({
      next: (data) => {
        this.ordenDetalles = data || [];
        console.log('[OrdenDetalleList] loaded ordenes detalles:', this.ordenDetalles.length, this.ordenDetalles);
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[OrdenDetalleList] failed to load ordenes detalles', err);
        this.ordenDetalles = [];
        if (err?.status === 0) {
          console.log('Cannot reach backend (network error). Is the Spring server running?');
        } else if (err?.status) {
          console.log(`Backend error ${err.status}: ${err?.message || err?.statusText || 'unknown'}`);
        } else {
          console.log('Unexpected error loading ordenes (check console)');
        }
      }
    });

    // Load payments (now extracted so we can refresh ONLY payments in real-time)
    this.loadPagos();
  }

  // NEW: Dedicated method to reload only payments (used by real-time + manual actions)
  private loadPagos(): void {
    if (!this.orden?.id) {
      this.pagos = [];
      return;
    }

    this.ordenPagoService.getPagosByOrden(this.orden.id).subscribe({
      next: (pagos) => {
        this.pagos = pagos || [];
        console.log('[OrdenDetalleList] loaded payments (real-time):', this.pagos.length);
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[OrdenDetalleList] failed to load pagos', err);
        this.pagos = [];
      }
    });
  }

  openRegistrarAdelantoModal() {
    this.nuevoPago = { monto: 0, metodoPago: 'Efectivo', codigoReferencia: '', banco: '', notas: '', tipoPago: 'Adelanto' };
    this.showRegistrarAdelantoModal = true;
  }

  // Clear reference & bank when user changes payment method
  onMetodoPagoChange() {
    if (this.nuevoPago.metodoPago !== 'Transferencia') {
      this.nuevoPago.codigoReferencia = '';
      this.nuevoPago.banco = '';
    }
  }

  registrarAdelanto() {
    if (!this.orden?.id || this.nuevoPago.monto <= 0) return;

    // Validacion para transferencia
    if (this.nuevoPago.metodoPago === 'Transferencia') {
      if (!this.nuevoPago.codigoReferencia?.trim()) {
        alert('❌ El código de referencia es obligatorio cuando el método es Transferencia.');
        return;
      }
      if (!this.nuevoPago.banco || this.nuevoPago.banco === '') {
        alert('❌ Debe seleccionar un banco cuando el método es Transferencia.');
        return;
      }
    }

    this.ordenPagoService.registrarPago(this.orden.id, this.nuevoPago).subscribe({
      next: () => {
        this.showRegistrarAdelantoModal = false;
        this.loadOrdenesDetalles(); // full refresh
      },
      error: (err) => {
        console.error('Error registrando adelanto', err);
        alert('No se pudo registrar el adelanto');
      }
    });
  }

  downloadPdf(): void {
    if (!this.orden?.id) return;

    this.ordenDetalleService.getPdf(this.orden.id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `orden-${this.orden!.id}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Error al descargar PDF', err);
        alert('No se pudo generar el PDF. Revisa la consola.');
      }
    });
  }

  deleteOrdenDetalle(idOrden: number, idOrdenDetalle: number): void {
    if (confirm('¿Seguro desea eliminar este detalle de orden?')) {
      this.ordenDetalleService.delete(idOrden, idOrdenDetalle).subscribe({
        next: () => {
          console.log('Detalle eliminado correctamente');
          this.loadOrdenesDetalles();
        },
        error: (err) => {
          console.error('Error al eliminar detalle', err);
          alert('No se pudo eliminar el detalle. Ver consola para más detalles.');
        }
      });
    }
  }

  goBack(): void {
    const from = this.route.snapshot.queryParams['from'];
    const fecha = this.route.snapshot.queryParams['fecha'];
    if (from === 'calendario' && fecha) {
      this.router.navigate(['/ordenes-calendario', fecha, 'new']);
    } else {
      this.router.navigate(['/ordenes']);
    }
  }
}