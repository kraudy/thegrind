import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { ProductoTipoEstado } from '../../productos-tipo-estados/producto-tipo-estado.model';
import { OrdenSeguimientoDetalleEntrega } from '../orden-seguimiento-detalle-entrega.model';

import { NotificationService } from '../../shared/notification.service';
import { ToastService } from '../../shared/toast/toast.service';

import { OrdenPagoService } from '../../ordenes-pago/orden-pago.service';
import { OrdenPago } from '../../ordenes-pago/orden-pago.model';
import { FacturaService } from '../../facturas/factura.service';
import { OrdenService } from '../../ordenes/orden.service';

@Component({
  selector: 'app-orden-seguimiento-entrega-detalle-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orden-seguimiento-entrega-detalle-list.html',
  styleUrls: ['./orden-seguimiento-entrega-detalle-list.css'],
})
export class OrdenSeguimientoEntregaDetalleListComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  idOrden!: number;
  detalles: OrdenSeguimientoDetalleEntrega[] = [];
  clienteNombre = 'Cargando...';

  // Estado de la orden a nivel cabecera (Listo o Entregado) - llega por queryParam
  ordenEstado: string = '';

  // Totales de facturacion - llegan por queryParams desde el listado
  totalProductosOrden: number = 0;
  totalMontoOrden: number = 0;
  totalProductosFactura: number = 0;
  totalMontoFactura: number = 0;
  saldoPendiente: number = 0;

  // Pagos registrados
  pagos: OrdenPago[] = [];

  // Stepper data por detalle
  possibleStatesMap = new Map<number, ProductoTipoEstado[]>();
  historyMap = new Map<number, any[]>();
  currentStateMap = new Map<number, string>();
  isOrderCompleted = false;

  // Modal pago / boton factura
  showRegistrarSaldoModal = false;
  creatingFactura = false;

  nuevoPago = {
    monto: 0,
    metodoPago: 'Efectivo',
    codigoReferencia: '',
    banco: '',
    notas: ''
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: OrdenSeguimientoService,
    private notificationService: NotificationService,
    private toastService: ToastService,
    private cd: ChangeDetectorRef,
    private ordenPagoService: OrdenPagoService,
    private facturaService: FacturaService,
    private ordenService: OrdenService
  ) {}

  ngOnInit() {
    this.notificationService.connect();

    this.notificationService.refreshNeeded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(view => {
        if (view === 'seguimiento' || view === 'pago') {
          console.log('🔄 Real-time refresh triggered for entrega/facturacion detail');
          this.loadAll();
        }
      });

    this.idOrden = Number(this.route.snapshot.paramMap.get('idOrden'));
    this.clienteNombre = String(this.route.snapshot.paramMap.get('clienteNombre'));

    const qp = this.route.snapshot.queryParamMap;
    this.ordenEstado = qp.get('estado') || '';
    this.totalProductosOrden = Number(qp.get('totalProductosOrden')) || 0;
    this.totalMontoOrden = Number(qp.get('totalMontoOrden')) || 0;
    this.totalProductosFactura = Number(qp.get('totalProductosFactura')) || 0;
    this.totalMontoFactura = Number(qp.get('totalMontoFactura')) || 0;
    this.saldoPendiente = Number(qp.get('saldoPendiente')) || 0;

    this.loadAll();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadAll() {
    this.loadOrdenEstado();
    this.loadDetalles();
    this.loadPagos();
  }

  private loadOrdenEstado() {
    if (!this.idOrden) return;
    this.ordenService.getById(this.idOrden).subscribe({
      next: (orden) => {
        this.ordenEstado = orden?.estado || this.ordenEstado;
        this.recalcSaldoPendiente();
        this.cd.detectChanges();
      },
      error: (err) => console.error('Error cargando estado de orden', err)
    });
  }

  private loadDetalles() {
    this.service.getOrdenDetalleParaEntrega(this.idOrden).subscribe({
      next: (data) => {
        this.detalles = data || [];
        this.loadStepperDataForAll();
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('❌ Error cargando detalle de entrega', err);
        this.toastService.showToast('error', 'Error al cargar', 'No se pudo cargar la informacion de la orden', 6000);
      },
    });
  }

  private loadPagos() {
    if (!this.idOrden) return;
    this.ordenPagoService.getPagosByOrden(this.idOrden).subscribe({
      next: (pagos) => {
        this.pagos = pagos || [];
        this.recalcSaldoPendiente();
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando pagos', err);
        this.pagos = [];
      }
    });
  }

  private recalcSaldoPendiente() {
    const totalAPagar = this.ordenEstado === 'Entregado'
      ? (this.totalMontoFactura || 0)
      : (this.totalMontoOrden || 0);
    const totalPagado = (this.pagos || []).reduce((acc, p) => acc + Number(p.monto || 0), 0);
    const pendiente = totalAPagar - totalPagado;
    this.saldoPendiente = pendiente > 0 ? Number(pendiente.toFixed(2)) : 0;
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

    if (this.detalles.length === 0) {
      this.isOrderCompleted = true;
    }
  }

  advanceDetail(det: OrdenSeguimientoDetalleEntrega) {
    let cantidad = 0;
    if (det.tipoProducto === 'Molduras' || det.tipoProducto === 'Retablos' || det.tipoProducto === 'Tabla' ||
      det.tipoProducto === 'Baner' || det.tipoProducto === 'Calado' || det.tipoProducto === 'Camisa' ||
      det.tipoProducto === 'Taza' || det.tipoProducto === 'Llavero'
    ) {
      cantidad = det.cantidadTrabajadaActual;
    } else {
      cantidad = det.cantidadTrabajadaPrevio;
    }

    this.service.progresoTrabajo(det.idOrden, det.idOrdenDetalle, cantidad).subscribe({
      next: () => {
        this.loadAll();
      },
      error: (err) => {
        console.error('Error adding progress:', err);
        this.toastService.showToast(
          'error',
          'Error al agregar progreso',
          'No se pudo registrar el progreso del trabajo. Verifique la cantidad.',
          7000
        );
        return;
      }
    });

    this.service.advance(det.idOrden, det.idOrdenDetalle).subscribe(() => {
      this.loadAll();
      this.toastService.showToast(
        'success',
        'Estado avanzado',
        `Detalle #${det.idOrdenDetalle} (${det.nombreProducto || 'Sin nombre'}) avanzo a Entregado correctamente`,
        4000
      );
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

  // ==================== Pagos ====================
  openRegistrarSaldoModal() {
    this.nuevoPago = {
      monto: this.saldoPendiente > 0 ? this.saldoPendiente : 0,
      metodoPago: 'Efectivo',
      codigoReferencia: '',
      banco: '',
      notas: ''
    };
    this.showRegistrarSaldoModal = true;
  }

  onMetodoPagoChange() {
    if (this.nuevoPago.metodoPago !== 'Transferencia') {
      this.nuevoPago.codigoReferencia = '';
      this.nuevoPago.banco = '';
    }
  }

  registrarSaldo() {
    if (!this.idOrden || this.nuevoPago.monto <= 0) {
      this.toastService.showToast('warning', 'Monto invalido', 'El monto debe ser mayor a 0.', 5000);
      return;
    }

    if (this.nuevoPago.metodoPago === 'Transferencia') {
      if (!this.nuevoPago.codigoReferencia?.trim()) {
        this.toastService.showToast('warning', 'Referencia requerida',
          'El codigo de referencia es obligatorio cuando el metodo es Transferencia.', 5000);
        return;
      }
      if (!this.nuevoPago.banco || this.nuevoPago.banco === '') {
        this.toastService.showToast('warning', 'Banco requerido',
          'Debe seleccionar un banco cuando el metodo es Transferencia.', 5000);
        return;
      }
    }

    this.ordenPagoService.registrarPago(this.idOrden, this.nuevoPago).subscribe({
      next: () => {
        this.showRegistrarSaldoModal = false;
        this.toastService.showToast(
          'success', 'Pago registrado',
          `Se registro un pago por C$ ${this.nuevoPago.monto.toFixed(2)}`, 5000
        );
        this.loadAll();
      },
      error: (err) => {
        console.error('Error registrando pago', err);
        const backendMessage = err?.error?.message || err?.message || 'No se pudo registrar el pago.';
        this.toastService.showToast('error', 'Error al registrar pago', backendMessage, 7000);
        this.cd.detectChanges();
      }
    });
  }

  // ==================== Factura ====================
  canGenerarFactura(): boolean {
    return this.ordenEstado === 'Entregado' && !this.creatingFactura;
  }

  generarFactura(): void {
    if (this.creatingFactura || !this.idOrden) return;

    if (!this.canGenerarFactura()) {
      this.toastService.showToast(
        'warning', 'Orden no entregada',
        'La orden debe estar completamente Entregada antes de facturar.', 5000
      );
      return;
    }

    if (!confirm('¿Desea generar la factura con las cantidades entregadas?')) return;

    this.creatingFactura = true;
    this.facturaService.createFromOrden(this.idOrden).subscribe({
      next: (factura) => {
        this.creatingFactura = false;
        this.toastService.showToast(
          'success', 'Factura creada',
          `Se creo la factura #${factura.id} para la orden #${this.idOrden}.`, 5000
        );
        this.router.navigate(['/ordenes-seguimiento-entrega']);
      },
      error: (err) => {
        console.error('Error creando factura', err);
        this.creatingFactura = false;
        this.toastService.showToast(
          'error', 'Error al crear factura',
          err?.error?.message || 'No se pudo crear la factura para esta orden.', 7000
        );
        this.cd.detectChanges();
      }
    });
  }

  goBack() {
    this.router.navigate(['/ordenes-seguimiento-entrega']);
  }
}
