import { Component, OnDestroy, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';

import { OrdenCalendario } from '../orden-calendario.model';
import { CalendarioDiaDTO } from '../calendario-dia.model';
import { OrdenCalendarioService } from '../orden-calendario.service';

import { NotificationService } from '../../shared/notification.service';
import { takeUntil } from 'rxjs/internal/operators/takeUntil';
import { Subject } from 'rxjs/internal/Subject';

@Component({
  selector: 'app-orden-calendario-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './orden-calendario-list.html',
  styleUrls: ['./orden-calendario-list.css'],
})
export class OrdenCalendarioListComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();
  
  thisWeekDays: CalendarioDiaDTO[] = [];
  nextWeekDays: CalendarioDiaDTO[] = [];

  estadisticas: any = {
    ordenesRecibidas: 0,
    reparadores: [] as {trabajador: string, cantidadDetalles: number}[],
    normales: [] as {trabajador: string, cantidadDetalles: number}[],
    repartidas: [] as {trabajador: string, cantidadDetalles: number}[],
    impresionNormal: 0,
    impresionReparacion: 0
  };

  constructor(
    private ordenCalendarioService: OrdenCalendarioService,
    private cd: ChangeDetectorRef,
    private router: Router,
    private notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.notificationService.connect();
    
        // Real-time refresh
        this.notificationService.refreshNeeded$
          .pipe(takeUntil(this.destroy$))
          .subscribe(view => {
            if (view === 'calendario') {
              console.log('🔄 Real-time refresh triggered for Calendario');
              this.loadEstadisticas();
            } else if (view === 'seguimiento') {
              console.log('🔄 Real-time refresh triggered for Seguimiento');
              this.loadCalendario();
            }
          });
          
    this.loadCalendario();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadCalendario() {
    this.ordenCalendarioService.getCalendario().subscribe({
      next: (data) => {
        this.thisWeekDays = data
          .filter(d => d.weekLabel === 'this week')
          .sort((a, b) => a.date.localeCompare(b.date));

        this.nextWeekDays = data
          .filter(d => d.weekLabel === 'next week')
          .sort((a, b) => a.date.localeCompare(b.date));

        this.cd.detectChanges();
      },
      error: (err) => console.error('Error cargando calendario', err)
    });

    this.loadEstadisticas();
  }

  loadEstadisticas() {
    this.ordenCalendarioService.getEstadisticasHoy().subscribe({
      next: (stats) => {
        this.estadisticas = stats;
        this.cd.detectChanges();
      },
      error: (err) => console.error('Error cargando estadísticas', err)
    });
  }

  goToScheduleDay(dateStr: string): void {
    this.router.navigate(['/ordenes-calendario', dateStr, 'new']);
  }

  getLoadColor(count: number): string {
    if (count === 0) return 'bg-gray-100 text-gray-400';
    if (count <= 10) return 'bg-green-100 text-green-700 border-green-200';
    if (count <= 20) return 'bg-yellow-100 text-yellow-700 border-yellow-200';
    return 'bg-red-100 text-red-700 border-red-200';
  }

  // Safe date creation (prevents timezone shift)
  formatDayName(dateStr: string): string {
    const [year, month, day] = dateStr.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    return date.toLocaleDateString('es-NI', { weekday: 'long' });
  }

  formatFullDate(dateStr: string): string {
    const [year, month, day] = dateStr.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    return date.toLocaleDateString('es-NI', { day: 'numeric', month: 'short' });
  }

  formatTime(hora: number, minuto: number): string {
    return `${hora.toString().padStart(2, '0')}:${minuto.toString().padStart(2, '0')}`;
  }

  isPastDay(dateStr: string): boolean {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const [year, month, day] = dateStr.split('-').map(Number);
    const dayDate = new Date(year, month - 1, day);
    return dayDate < today;
  }
}