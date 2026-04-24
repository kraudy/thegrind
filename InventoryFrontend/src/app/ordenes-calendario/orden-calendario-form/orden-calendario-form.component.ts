import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { OrdenCalendario } from '../orden-calendario.model';
import { OrdenCalendarioService } from '../orden-calendario.service';

import { Orden } from '../../ordenes/orden.model';
import { OrdenService } from '../../ordenes/orden.service';

import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-orden-calendario-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orden-calendario-form.html',
  styleUrls: ['./orden-calendario-form.css'],
})
export class OrdenCalendarioFormComponent implements OnInit {
  selectedDate!: Date;
  dateStr: string = '';
  pendingOrders: Orden[] = [];
  selectedOrderIds: number[] = [];
  alreadyScheduledCount = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ordenService: OrdenService,
    private calendarioService: OrdenCalendarioService,
    private cd: ChangeDetectorRef,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.dateStr = this.route.snapshot.paramMap.get('fecha')!;
    this.selectedDate = new Date(this.dateStr + 'T00:00:00');

    this.loadPendingOrders();
    this.loadAlreadyScheduledCount();
  }

  private loadPendingOrders(): void {
    this.ordenService.getRecibidas().subscribe({
      next: (data) => {
        this.pendingOrders = data || [];
        console.log('✅ Pending orders loaded:', this.pendingOrders.length, this.pendingOrders);
        this.cd.detectChanges();          // ← Fuerza la actualización de la vista
      },
      error: (err) => {
        console.error('❌ Error cargando recibidas', err);
        this.pendingOrders = [];
        this.toastService.showToast('error', 'Error al cargar', 'No se pudieron cargar las órdenes pendientes', 6000);
        this.cd.detectChanges();
      }
    });
  }

  private loadAlreadyScheduledCount(): void {
    this.calendarioService.getByDate(this.dateStr).subscribe({
      next: (data) => {
        this.alreadyScheduledCount = data || 0;
        this.cd.detectChanges();
      },
      error: () => {
        this.alreadyScheduledCount = 0;
        this.toastService.showToast('error', 'Error al cargar', 'No se pudo cargar el conteo de órdenes programadas', 6000);
        this.cd.detectChanges();
      }
    });
  }

  formatDayName(): string {
    const [year, month, day] = this.dateStr.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    return date.toLocaleDateString('es-NI', { weekday: 'long' });
  }

  formatFullDate(): string {
    const [year, month, day] = this.dateStr.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    // Incluye el año para mayor claridad en esta vista
    return date.toLocaleDateString('es-NI', { 
      day: 'numeric', 
      month: 'short', 
      year: 'numeric' 
    });
  }

  toggleOrder(id: number): void {
    if (this.selectedOrderIds.includes(id)) {
      this.selectedOrderIds = this.selectedOrderIds.filter(x => x !== id);
    } else {
      this.selectedOrderIds.push(id);
    }
  }

  selectAllOrders(): void {
    if (this.selectedOrderIds.length === this.pendingOrders.length) {
      // If all are selected, deselect all
      this.selectedOrderIds = [];
    } else {
      // Select all
      this.selectedOrderIds = this.pendingOrders.map(order => order.id!).filter(id => id !== undefined);
    }
  }

  scheduleSelected(): void {
    if (this.selectedOrderIds.length === 0) return;

    const fechaTrabajo = new Date(this.selectedDate);
    fechaTrabajo.setHours(8, 0, 0, 0);

    const requests = this.selectedOrderIds.map((idOrden, index) => {
      const cal: Partial<OrdenCalendario> = {
        idOrden: idOrden,
        fechaTrabajo: new Date(fechaTrabajo.getTime() + index * 60000).toISOString()
      };
      return this.calendarioService.create(cal);
    });

    Promise.all(requests.map(r => r.toPromise())).then(() => {
      this.toastService.showToast('success', 'Órdenes programadas', `Se programaron ${this.selectedOrderIds.length} órdenes para el ${this.formatFullDate()}`, 4000);
      this.router.navigate(['/ordenes-calendario']);
    }).catch(err => {
      console.error(err);
      this.toastService.showToast('error', 'Error al guardar', 'No se pudieron guardar las órdenes en el calendario', 7000);
    });
  }

  goBack(): void {
    this.router.navigate(['/ordenes-calendario']);
  }

  viewDetails(ordenId: number): void {
    this.router.navigate(['/ordenes-detalle', ordenId], { queryParams: { from: 'calendario', fecha: this.dateStr } });
  }
}