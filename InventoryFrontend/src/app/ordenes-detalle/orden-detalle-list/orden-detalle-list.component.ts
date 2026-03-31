import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms'; 

import { OrdenDetalleService } from '../orden-detalle.service';
import { OrdenDetalle } from '../orden-detalle.model';

import { Orden } from '../../ordenes/orden.model';
import { OrdenService } from '../../ordenes/orden.service';

import { OrdenPago } from '../../ordenes-pago/orden-pago.model';

@Component({
  selector: 'app-orden-detalle-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './orden-detalle-list.html',
  styleUrls: ['./orden-detalle-list.css'],
})
export class OrdenDetalleListComponent implements OnInit, OnChanges {
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
    notas: ''
  };

  constructor(
      private ordenDetalleService: OrdenDetalleService,
      private ordenService: OrdenService,      // ← Nuevo
      private route: ActivatedRoute,
      private router: Router,
      private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (!this.orden) {
      this.loadOrdenFromRoute();
    } else {
      this.loadOrdenesDetalles();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Soporte para cuando se pasa [orden] como input (ej: en una vista padre)
    if (changes['orden'] && this.orden?.id) {
      this.loadOrdenesDetalles();
    }
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
      this.pagos = [];           // ← clear payments too
      return;
    }
    // It must receive the order as input and load only the details for that order
    this.ordenDetalleService.getByOrden(this.orden.id).subscribe({
      next: (data) => {
        this.ordenDetalles = data || [];
        console.log('[OrdenDetalleList] loaded ordenes detalles:', this.ordenDetalles.length, this.ordenDetalles);
        this.cd.detectChanges(); // force view update if zone isn't triggering it
      },
      error: (err) => {
        // log full error to inspect status/code in the console
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

    // 2. NEW: Load payments for this order
    this.ordenDetalleService.getPagosByOrden(this.orden.id).subscribe({
      next: (pagos) => {
        this.pagos = pagos || [];
        console.log('[OrdenDetalleList] loaded payments:', this.pagos.length);
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[OrdenDetalleList] failed to load pagos', err);
        this.pagos = [];
      }
    });

  }

  openRegistrarAdelantoModal() {
    this.nuevoPago = { monto: 0, metodoPago: 'Efectivo', codigoReferencia: '', banco: '', notas: '' };
    this.showRegistrarAdelantoModal = true;
  }

  registrarAdelanto() {
    if (!this.orden?.id || this.nuevoPago.monto <= 0) return;

    this.ordenDetalleService.registrarPago(this.orden.id, this.nuevoPago).subscribe({
      next: () => {
        this.showRegistrarAdelantoModal = false;
        this.loadOrdenesDetalles(); // refresh payments
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
