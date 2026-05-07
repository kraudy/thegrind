import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { OrdenSeguimientoService } from '../../ordenes-seguimiento/orden-seguimiento.service';

import { NotificationService } from '../../shared/notification.service'; 
import { ToastService } from '../../shared/toast/toast.service';

import { OrdenFacturacionDetalle } from '../orden-facturacion-detalle.model';

import { OrdenPagoService } from '../../ordenes-pago/orden-pago.service';
import { OrdenPago } from '../../ordenes-pago/orden-pago.model';
import { FacturaService } from '../../facturas/factura.service';

@Component({
  selector: 'app-orden-facturacion-detalle-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orden-facturacion-detalle-list.html',
  styleUrls: ['./orden-facturacion-detalle-list.css'],
})
export class OrdenFacturacionDetalleListComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  idOrden!: number;
  detalles: OrdenFacturacionDetalle[] = [];
  clienteNombre = 'Cargando...';

  totalProductosOrden: number = 0;
  totalMontoOrden: number = 0;
  totalProductosFactura: number = 0;
  totalMontoFactura: number = 0;

  // ==================== NUEVO ====================
  saldoPendiente: number = 0;

  // ==================== PAGOS ====================
  pagos: OrdenPago[] = [];

  // ==================== Modal Saldo ====================
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
    private facturaService: FacturaService
  ) {}

  ngOnInit() {
    this.notificationService.connect();

    this.notificationService.refreshNeeded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(view => {
        if (view === 'seguimiento' || view === 'pago') {
          console.log('🔄 Refresh triggered in facturación');
          this.loadAll();
        }
      });

    this.idOrden = Number(this.route.snapshot.paramMap.get('idOrden'));
    this.clienteNombre = String(this.route.snapshot.queryParamMap.get('clienteNombre') || this.route.snapshot.paramMap.get('clienteNombre'));
    
    // Leer todos los valores desde query params
    this.totalProductosOrden = Number(this.route.snapshot.queryParamMap.get('totalProductosOrden')) || 0;
    this.totalMontoOrden = Number(this.route.snapshot.queryParamMap.get('totalMontoOrden')) || 0;
    this.totalProductosFactura = Number(this.route.snapshot.queryParamMap.get('totalProductosFactura')) || 0;
    this.totalMontoFactura = Number(this.route.snapshot.queryParamMap.get('totalMontoFactura')) || 0;
    this.saldoPendiente = Number(this.route.snapshot.queryParamMap.get('saldoPendiente')) || 0;   // ← NUEVO

    this.loadAll();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadAll(): void {
    this.loadDetalles();
    this.loadPagos();
  }

  private loadDetalles(): void {
    this.service.getOrdenDetalleParaFacturacion(this.idOrden).subscribe({
      next: (data) => {
        this.detalles = data || [];
        this.cd.detectChanges();
      },
      error: (err) => console.error('❌ Error cargando detalle de facturación', err),
    });
  }

  private loadPagos(): void {
    if (!this.idOrden) return;
    this.ordenPagoService.getPagosByOrden(this.idOrden).subscribe({
      next: (pagos) => {
        this.pagos = pagos || [];
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando pagos', err);
        this.pagos = [];
      }
    });
  }

  // ==================== Modal Saldo (con prellenado automático) ====================
  openRegistrarSaldoModal() {
    this.nuevoPago = {
      monto: this.saldoPendiente,           // ← PRELLENADO AUTOMÁTICO
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
      this.toastService.showToast(
        'warning',
        'Monto invalido',
        'El monto debe ser mayor a 0.',
        5000
      );
      return;
    }

    if (this.nuevoPago.metodoPago === 'Transferencia') {
      if (!this.nuevoPago.codigoReferencia?.trim()) {
        this.toastService.showToast(
          'warning',
          'Referencia requerida',
          'El codigo de referencia es obligatorio cuando el metodo es Transferencia.',
          5000
        );
        return;
      }
      if (!this.nuevoPago.banco || this.nuevoPago.banco === '') {
        this.toastService.showToast(
          'warning',
          'Banco requerido',
          'Debe seleccionar un banco cuando el metodo es Transferencia.',
          5000
        );
        return;
      }
    }

    this.ordenPagoService.registrarPago(this.idOrden, this.nuevoPago).subscribe({
      next: () => {
        this.showRegistrarSaldoModal = false;

        // ← Beautiful toast instead of alert
        this.toastService.showToast(
          'success',
          'Pago registrado',
          `Se registró un pago de saldo por C$ ${this.nuevoPago.monto.toFixed(2)}`,
          5000
        );

        this.loadAll();
      },
      error: (err) => {
        console.error('Error registrando saldo', err);

        const backendMessage = err?.error?.message || err?.message || 'No se pudo registrar el pago de saldo.';
        this.toastService.showToast(
          'error',
          'Error al registrar pago',
          backendMessage,
          7000
        );
        this.cd.detectChanges();
      }
    });
  }

  generarFactura(): void {
    if (this.creatingFactura || !this.idOrden) {
      return;
    }

    if (!confirm('¿Desea generar la factura con las cantidades entregadas?')) {
      return;
    }

    this.creatingFactura = true;
    this.facturaService.createFromOrden(this.idOrden).subscribe({
      next: (factura) => {
        this.creatingFactura = false;
        this.toastService.showToast(
          'success',
          'Factura creada',
          `Se creó la factura #${factura.id} para la orden #${this.idOrden}.`,
          5000
        );
        this.router.navigate(['/ordenes-facturacion']);
      },
      error: (err) => {
        console.error('Error creando factura', err);
        this.creatingFactura = false;
        this.toastService.showToast(
          'error',
          'Error al crear factura',
          err?.error?.message || 'No se pudo crear la factura para esta orden.',
          7000
        );
        this.cd.detectChanges();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/ordenes-facturacion']);
  }
}