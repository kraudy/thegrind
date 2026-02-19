import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { OrdenCalendario } from '../orden-calendario.model';
import { OrdenCalendarioService } from '../orden-calendario.service';

import { Orden } from '../../ordenes/orden.model';
import { OrdenService } from '../../ordenes/orden.service';

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
    private cd: ChangeDetectorRef   // ← ¡Esto era lo que faltaba!
  ) {}

  ngOnInit(): void {
    this.dateStr = this.route.snapshot.paramMap.get('fecha')!;
    this.selectedDate = new Date(this.dateStr + 'T00:00:00');

    this.loadPendingOrders();
    this.loadAlreadyScheduledCount();
  }

  private loadPendingOrders(): void {
    this.ordenService.getPendientes().subscribe({
      next: (data) => {
        this.pendingOrders = data || [];
        console.log('✅ Pending orders loaded:', this.pendingOrders.length, this.pendingOrders);
        this.cd.detectChanges();          // ← Fuerza la actualización de la vista
      },
      error: (err) => {
        console.error('❌ Error cargando pendientes', err);
        this.pendingOrders = [];
        this.cd.detectChanges();
      }
    });
  }

  private loadAlreadyScheduledCount(): void {
    this.calendarioService.getByDate(this.dateStr).subscribe({
      next: (data) => {
        this.alreadyScheduledCount = data.length;
        this.cd.detectChanges();
      },
      error: () => {
        this.alreadyScheduledCount = 0;
        this.cd.detectChanges();
      }
    });
  }

  toggleOrder(id: number): void {
    if (this.selectedOrderIds.includes(id)) {
      this.selectedOrderIds = this.selectedOrderIds.filter(x => x !== id);
    } else {
      this.selectedOrderIds.push(id);
    }
  }

  scheduleSelected(): void {
    if (this.selectedOrderIds.length === 0) return;

    const fechaTrabajo = new Date(this.selectedDate);
    fechaTrabajo.setHours(8, 0, 0, 0);

    const requests = this.selectedOrderIds.map((idOrden, index) => {
      const cal: Partial<OrdenCalendario> = {
        idOrden: idOrden,
        fechaTrabajo: new Date(fechaTrabajo.getTime() + index * 60000).toISOString(),
        usuarioCreacion: 'adminTest',           // ← Cambia por tu usuario real cuando tengas login
        usuarioModificacion: 'adminTest'
      };
      return this.calendarioService.create(cal);
    });

    Promise.all(requests.map(r => r.toPromise())).then(() => {
      alert(`¡${this.selectedOrderIds.length} órdenes programadas con éxito!`);
      this.router.navigate(['/ordenes-calendario']);
    }).catch(err => {
      console.error(err);
      alert('Error al guardar en el calendario');
    });
  }

  goBack(): void {
    this.router.navigate(['/ordenes-calendario']);
  }
}