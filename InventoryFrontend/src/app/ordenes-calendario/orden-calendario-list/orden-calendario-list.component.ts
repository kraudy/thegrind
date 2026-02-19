import { Component, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';

import { OrdenCalendario } from '../orden-calendario.model';
import { OrdenCalendarioService } from '../orden-calendario.service';

@Component({
  selector: 'app-orden-calendario-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './orden-calendario-list.html',
  styleUrls: ['./orden-calendario-list.css'],
})
export class OrdenCalendarioListComponent implements OnInit {
  currentWeek: Date[] = [];
  nextWeek: Date[] = [];

  scheduledByDay: { [key: string]: OrdenCalendario[] } = {}; // clave = YYYY-MM-DD

  constructor(
    private ordenCalendarioService: OrdenCalendarioService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.generateWeeks();
    this.loadCalendario();
  }

  private generateWeeks() {
    const today = new Date();
    const startCurrent = this.getMonday(today);

    this.currentWeek = Array.from({ length: 7 }, (_, i) => {
      const d = new Date(startCurrent);
      d.setDate(d.getDate() + i);
      return d;
    });

    const startNext = new Date(startCurrent);
    startNext.setDate(startNext.getDate() + 7);

    this.nextWeek = Array.from({ length: 7 }, (_, i) => {
      const d = new Date(startNext);
      d.setDate(d.getDate() + i);
      return d;
    });
  }

  private getMonday(d: Date): Date {
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1); // lunes
    return new Date(d.setDate(diff));
  }

  loadCalendario() {
    this.ordenCalendarioService.getAll().subscribe({
      next: (data) => {
        this.scheduledByDay = {};
        data.forEach(item => {
          const key = item.fecha; // YYYY-MM-DD que ya viene del backend
          if (!this.scheduledByDay[key]) this.scheduledByDay[key] = [];
          this.scheduledByDay[key].push(item);
        });
      },
      error: (err) => console.error('Error cargando calendario', err)
    });
  }
  

  goToScheduleDay(date: Date): void {
    const fechaStr = date.toISOString().split('T')[0]; // YYYY-MM-DD
    this.router.navigate(['/ordenes-calendario', fechaStr, 'new']);
  }

  getOrdersForDay(date: Date): OrdenCalendario[] {
    const key = date.toISOString().split('T')[0];
    return this.scheduledByDay[key] || [];
  }

  getLoadColor(count: number): string {
    if (count === 0) return 'bg-gray-100 text-gray-400';
    if (count <= 3) return 'bg-green-100 text-green-700 border-green-200';
    if (count <= 6) return 'bg-yellow-100 text-yellow-700 border-yellow-200';
    return 'bg-red-100 text-red-700 border-red-200';
  }

  formatDayName(date: Date): string {
    return date.toLocaleDateString('es-NI', { weekday: 'short' }).toUpperCase();
  }

  formatFullDate(date: Date): string {
    return date.toLocaleDateString('es-NI', { day: 'numeric', month: 'short' });
  }

  formatTime(hora: number, minuto: number): string {
    return `${hora.toString().padStart(2, '0')}:${minuto.toString().padStart(2, '0')}`;
  }
}
