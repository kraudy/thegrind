import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { OrdenPagoService } from './orden-pago.service';   // reuse the service
import { NotificationService } from '../shared/notification.service';

import { OrdenPago } from './orden-pago.model';

@Component({
  selector: 'app-orden-pago-aprobar-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './orden-pago-aprobar-list.html',
  styleUrls: ['./orden-pago-aprobar-list.css'],
})
export class OrdenPagoAprobarListComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  pagos: OrdenPago[] = [];
  loading = false;
  errorMessage = '';

  searchTerm = '';
  selectedEstadoFilter = '';   // '' = Todos, 'Pendiente', 'Aprobado'

  constructor(
    private ordenPagoService: OrdenPagoService,
    private notificationService: NotificationService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.notificationService.connect();

    this.notificationService.refreshNeeded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(view => {
        if (view === 'pago') {           // ← Cambiado de 'seguimiento' a 'pago'
          console.log('🔄 Refresh triggered by WebSocket (pagos)');
          this.loadPagos();
        }
      });

    this.loadPagos();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadPagos(): void {
    this.loading = true;
    this.errorMessage = '';

    this.ordenPagoService.getPagosPendientes(this.searchTerm, this.selectedEstadoFilter)
      .subscribe({
        next: (data) => {
          this.pagos = data || [];
          this.loading = false;
          this.cd.detectChanges();
        },
        error: (err: any) => {
          console.error('[OrdenPagoAprobarList] error', err);
          this.pagos = [];
          this.loading = false;
          this.errorMessage = 'No se pudo cargar los adelantos.';
          this.cd.detectChanges();
        }
      });
  }

  onFilterChange() {
    this.loadPagos();
  }

  clearFilters() {
    this.searchTerm = '';
    this.selectedEstadoFilter = '';
    this.loadPagos();
  }

  aprobarPago(id: number) {
    this.ordenPagoService.aprobarPago(id).subscribe({
      next: () => {
        this.loadPagos();
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error aprobando', err);
        alert('Error aprobando el adelanto');
      }
    });
  }

  rechazarPago(id: number) {
    if (confirm('¿Seguro que deseas rechazar este adelanto?')) {
      this.ordenPagoService.rechazarPago(id).subscribe({
        next: () => {
          this.loadPagos();
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error('Error rechazando', err);
          alert('Error rechazando el adelanto');
        }
      });
    }
  }
}