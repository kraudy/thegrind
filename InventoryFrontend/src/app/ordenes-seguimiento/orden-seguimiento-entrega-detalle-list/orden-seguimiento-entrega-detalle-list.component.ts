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

  // Estado de la orden a nivel cabecera (Listo o Entregado)
  ordenEstado: string = '';

  // Totales (computados reactivamente desde detalles + pagos)
  totalProductosOrden: number = 0;
  totalMontoOrden: number = 0;
  totalProductosFactura: number = 0;
  totalMontoFactura: number = 0;
  saldoPendiente: number = 0;

  // Input manual por detalle (cantidad a marcar como entregada en esta accion)
  cantidadAEntregarInputs: { [idOrdenDetalle: number]: number } = {};

  // Auto-advance: previene reintentos infinitos si el backend rechaza el avance
  private autoAdvanceSignatureMap: { [idOrdenDetalle: number]: string } = {};
  private advancingDetailMap: { [idOrdenDetalle: number]: boolean } = {};

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

    // Estado inicial (puede venir por queryParam para evitar parpadeo) — se refresca via loadOrdenEstado
    this.ordenEstado = this.route.snapshot.queryParamMap.get('estado') || '';

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
        this.recalcTotales();
        this.cd.detectChanges();
      },
      error: (err) => console.error('Error cargando estado de orden', err)
    });
  }

  private loadDetalles() {
    this.service.getOrdenDetalleParaEntrega(this.idOrden).subscribe({
      next: (data) => {
        this.detalles = data || [];
        // Inicializar input por detalle con la cantidad pendiente por entregar
        this.detalles.forEach(det => {
          const pendiente = Math.max(0, (det.cantidad || 0) - (det.cantidadEntregada || 0));
          if (this.cantidadAEntregarInputs[det.idOrdenDetalle] === undefined) {
            this.cantidadAEntregarInputs[det.idOrdenDetalle] = pendiente;
          }
        });
        this.loadStepperDataForAll();
        this.recalcTotales();
        this.cd.detectChanges();
        this.tryAutoAdvanceCompletedDetails();
      },
      error: (err) => {
        console.error('Error cargando detalle de entrega', err);
        this.toastService.showToast('error', 'Error al cargar', 'No se pudo cargar la informacion de la orden', 6000);
      },
    });
  }

  private loadPagos() {
    if (!this.idOrden) return;
    this.ordenPagoService.getPagosByOrden(this.idOrden).subscribe({
      next: (pagos) => {
        this.pagos = pagos || [];
        this.recalcTotales();
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando pagos', err);
        this.pagos = [];
      }
    });
  }

  /**
   * Unica fuente de verdad para los totales de la pantalla. Recalcula:
   *  - totalProductosOrden / totalMontoOrden  (basado en cantidad y precio de orden_detalle)
   *  - totalProductosFactura / totalMontoFactura (basado en cantidad_trabajada de orden_trabajo estado='Entregado')
   *  - saldoPendiente (contra el total relevante segun estado de la orden)
   */
  private recalcTotales() {
    const dets = this.detalles || [];
    this.totalProductosOrden = dets.reduce((acc, d) => acc + Number(d.cantidad || 0), 0);
    this.totalMontoOrden = dets.reduce((acc, d) =>
      acc + Number(d.cantidad || 0) * Number(d.precioUnitario || 0), 0);
    this.totalProductosFactura = dets.reduce((acc, d) => acc + Number(d.cantidadEntregada || 0), 0);
    this.totalMontoFactura = dets.reduce((acc, d) =>
      acc + Number(d.cantidadEntregada || 0) * Number(d.precioUnitario || 0), 0);

    const totalAPagar = this.ordenEstado === 'Entregado'
      ? this.totalMontoFactura
      : this.totalMontoOrden;
    const totalPagado = (this.pagos || []).reduce((acc, p) => acc + Number(p.monto || 0), 0);
    const pendiente = totalAPagar - totalPagado;
    this.saldoPendiente = pendiente > 0 ? Number(pendiente.toFixed(2)) : 0;
  }

  /** Subtotal por detalle para mostrar en la columna 'Orden' */
  subtotalOrden(det: OrdenSeguimientoDetalleEntrega): number {
    return Number(det.cantidad || 0) * Number(det.precioUnitario || 0);
  }

  /** Subtotal por detalle para mostrar en la columna 'Entregado' (lo que sera facturado) */
  subtotalEntregado(det: OrdenSeguimientoDetalleEntrega): number {
    return Number(det.cantidadEntregada || 0) * Number(det.precioUnitario || 0);
  }

  /** Cuantos quedan por entregar para este detalle */
  cantidadPendienteEntrega(det: OrdenSeguimientoDetalleEntrega): number {
    return Math.max(0, Number(det.cantidad || 0) - Number(det.cantidadEntregada || 0));
  }

  /** El detalle ya esta en estado Entregado a nivel seguimiento y todas las unidades fueron entregadas. */
  isFullyEntregado(det: OrdenSeguimientoDetalleEntrega): boolean {
    return det.estadoActual === 'Entregado'
      && Number(det.cantidad || 0) > 0
      && Number(det.cantidadEntregada || 0) >= Number(det.cantidad || 0);
  }

  /** El detalle esta en Entregado pero con cantidad menor a la pedida (entrega parcial cerrada). */
  isPartiallyEntregado(det: OrdenSeguimientoDetalleEntrega): boolean {
    return det.estadoActual === 'Entregado'
      && Number(det.cantidadEntregada || 0) > 0
      && Number(det.cantidadEntregada || 0) < Number(det.cantidad || 0);
  }

  /** trackBy para *ngFor de detalles: evita destruir/recrear el DOM (y por tanto el salto de scroll) en cada reload. */
  trackByDetalle = (_index: number, det: OrdenSeguimientoDetalleEntrega): number => det.idOrdenDetalle;

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

  /**
   * Registra cantidad entregada para un detalle (acumulativa en orden_trabajo estado='Entregado').
   * No avanza el estado; el usuario decide cuando avanzar via 'Marcar como Entregado'.
   */
  registrarEntrega(det: OrdenSeguimientoDetalleEntrega) {
    const cantidad = Number(this.cantidadAEntregarInputs[det.idOrdenDetalle] || 0);
    const pendiente = this.cantidadPendienteEntrega(det);

    if (cantidad <= 0) {
      this.toastService.showToast('warning', 'Cantidad invalida',
        'Ingrese una cantidad mayor a 0 para registrar la entrega.', 5000);
      return;
    }
    if (cantidad > pendiente) {
      this.toastService.showToast('warning', 'Cantidad excede pendiente',
        `Solo quedan ${pendiente} unidades por entregar para este detalle.`, 5000);
      return;
    }

    this.service.progresoTrabajo(det.idOrden, det.idOrdenDetalle, cantidad).subscribe({
      next: () => {
        this.cantidadAEntregarInputs[det.idOrdenDetalle] = 0;
        this.toastService.showToast('success', 'Entrega registrada',
          `Se registraron ${cantidad} unidades entregadas para #${det.idOrdenDetalle}.`, 4000);
        this.loadAll();
      },
      error: (err) => {
        console.error('Error registrando entrega:', err);
        const msg = err?.error?.message || 'No se pudo registrar la entrega. Verifique la cantidad.';
        this.toastService.showToast('error', 'Error al registrar entrega', msg, 7000);
      }
    });
  }

  /**
   * Avanza el detalle al siguiente estado (Listo -> Entregado a nivel seguimiento).
   * No requiere que toda la cantidad este entregada — el operador decide si entrega parcial cierra el detalle.
   */
  advanceDetail(det: OrdenSeguimientoDetalleEntrega) {
    if (!det.permiteMover) return;
    if (this.advancingDetailMap[det.idOrdenDetalle]) return;

    const entregada = Number(det.cantidadEntregada || 0);
    if (entregada <= 0) {
      this.toastService.showToast('warning', 'Sin cantidad entregada',
        `Registre al menos 1 unidad entregada antes de marcar el detalle como Entregado.`, 5000);
      return;
    }

    const pendiente = this.cantidadPendienteEntrega(det);
    if (pendiente > 0) {
      const ok = confirm(
        `Se entregaron ${entregada} de ${det.cantidad} unidades para #${det.idOrdenDetalle} (${det.nombreProducto}).\n` +
        `¿Marcar el detalle como Entregado con esta cantidad parcial?`
      );
      if (!ok) return;
    }

    this.advancingDetailMap[det.idOrdenDetalle] = true;
    this.service.advance(det.idOrden, det.idOrdenDetalle).subscribe({
      next: () => {
        this.advancingDetailMap[det.idOrdenDetalle] = false;
        this.loadAll();
        this.toastService.showToast('success', 'Detalle entregado',
          `Detalle #${det.idOrdenDetalle} (${det.nombreProducto || 'Sin nombre'}) avanzo a Entregado.`, 4000);
      },
      error: (err) => {
        this.advancingDetailMap[det.idOrdenDetalle] = false;
        console.error('Error avanzando estado:', err);
        const msg = err?.error?.message || 'No se pudo avanzar el estado del detalle.';
        this.toastService.showToast('error', 'Error al avanzar', msg, 7000);
      }
    });
  }

  /**
   * Si un detalle tiene cantidadEntregada >= cantidad pedida y aun esta en Listo,
   * lo avanza automaticamente a Entregado. Usa una firma para evitar bucles si el avance falla.
   */
  private tryAutoAdvanceCompletedDetails(): void {
    for (const det of this.detalles) {
      if (!this.shouldAutoAdvance(det)) continue;

      const signature = this.buildAutoAdvanceSignature(det);
      if (this.autoAdvanceSignatureMap[det.idOrdenDetalle] === signature) continue;

      this.autoAdvanceSignatureMap[det.idOrdenDetalle] = signature;
      this.advanceDetail(det);
      break; // loadAll() volvera a disparar este metodo para el siguiente detalle
    }
  }

  private shouldAutoAdvance(det: OrdenSeguimientoDetalleEntrega): boolean {
    if (!det.permiteMover) return false;
    if (this.advancingDetailMap[det.idOrdenDetalle]) return false;
    return Number(det.cantidad || 0) > 0
      && Number(det.cantidadEntregada || 0) >= Number(det.cantidad || 0);
  }

  private buildAutoAdvanceSignature(det: OrdenSeguimientoDetalleEntrega): string {
    return [det.idOrden, det.idOrdenDetalle, det.estadoActual, det.cantidad, det.cantidadEntregada, det.permiteMover].join('|');
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
  /** Suma de pagos aprobados (la unica fuente de verdad para autorizar facturacion). */
  totalPagadoAprobado(): number {
    return (this.pagos || [])
      .filter(p => p.estado === 'Aprobado')
      .reduce((acc, p) => acc + Number(p.monto || 0), 0);
  }

  /** Saldo pendiente contra el total factura, considerando solo pagos aprobados. */
  saldoFacturaPendiente(): number {
    const pendiente = Number(this.totalMontoFactura || 0) - this.totalPagadoAprobado();
    return pendiente > 0 ? Number(pendiente.toFixed(2)) : 0;
  }

  canGenerarFactura(): boolean {
    if (this.creatingFactura) return false;
    if (this.ordenEstado !== 'Entregado') return false;
    if (Number(this.totalMontoFactura || 0) <= 0) return false;
    return this.saldoFacturaPendiente() <= 0;
  }

  generarFacturaTooltip(): string {
    if (this.ordenEstado !== 'Entregado') {
      return 'Solo se puede facturar cuando la orden esta Entregada';
    }
    if (this.saldoFacturaPendiente() > 0) {
      return `Faltan C$ ${this.saldoFacturaPendiente().toFixed(2)} en pagos aprobados para cubrir la factura`;
    }
    return '';
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
