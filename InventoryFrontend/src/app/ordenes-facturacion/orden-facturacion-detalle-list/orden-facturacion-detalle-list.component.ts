import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { OrdenSeguimientoService } from '../../ordenes-seguimiento/orden-seguimiento.service';
import { NotificationService } from '../../shared/notification.service'; 
import { OrdenFacturacionDetalle } from '../orden-facturacion-detalle.model';

@Component({
  selector: 'app-orden-facturacion-detalle-list',
  standalone: true,
  imports: [CommonModule],   // ← removed RouterLink (not used)
  templateUrl: './orden-facturacion-detalle-list.html',
  styleUrls: ['./orden-facturacion-detalle-list.css'],
})
export class OrdenFacturacionDetalleListComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  idOrden!: number;
  detalles: OrdenFacturacionDetalle[] = [];
  clienteNombre = 'Cargando...';

  // Totals passed from list component
  totalProductosOrden: number = 0;
  totalMontoOrden: number = 0;
  totalProductosFactura: number = 0;
  totalMontoFactura: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: OrdenSeguimientoService,
    private notificationService: NotificationService, 
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.notificationService.connect();

    this.notificationService.refreshNeeded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(view => {
        if (view === 'seguimiento') {
          console.log('🔄 Real-time refresh triggered for facturación detail');
          this.load();
        }
      });

    this.idOrden = Number(this.route.snapshot.paramMap.get('idOrden'));
    this.clienteNombre = String(this.route.snapshot.queryParamMap.get('clienteNombre') || this.route.snapshot.paramMap.get('clienteNombre'));
    
    // Read totals from query params
    this.totalProductosOrden = Number(this.route.snapshot.queryParamMap.get('totalProductosOrden')) || 0;
    this.totalMontoOrden = Number(this.route.snapshot.queryParamMap.get('totalMontoOrden')) || 0;
    this.totalProductosFactura = Number(this.route.snapshot.queryParamMap.get('totalProductosFactura')) || 0;
    this.totalMontoFactura = Number(this.route.snapshot.queryParamMap.get('totalMontoFactura')) || 0;
    
    this.load();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load() {
    this.service.getOrdenDetalleParaFacturacion(this.idOrden).subscribe({
      next: (data) => {
        this.detalles = data || [];
        this.cd.detectChanges();
      },
      error: (err) => console.error('❌ Error cargando detalle de facturación', err),
    });
  }

  generarFactura(): void {
    if (confirm('¿Desea generar la factura con las cantidades entregadas?')) {
      // TODO: Replace with real service call when you create FacturaController
      alert('✅ Factura generada (backend pendiente). Los datos ya están listos.');
      this.load();
    }
  }

  goBack(): void {
    this.router.navigate(['/ordenes-facturacion']);
  }
}