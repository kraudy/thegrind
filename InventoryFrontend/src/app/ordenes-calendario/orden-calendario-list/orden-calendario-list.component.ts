import { Component, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';

import { OrdenCalendario } from '../orden-calendario.model';
import { CalendarioDiaDTO } from '../calendario-dia.model';
import { OrdenCalendarioService } from '../orden-calendario.service';

@Component({
  selector: 'app-orden-calendario-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './orden-calendario-list.html',
  styleUrls: ['./orden-calendario-list.css'],
})
export class OrdenCalendarioListComponent implements OnInit {

  thisWeekDays: CalendarioDiaDTO[] = [];
  nextWeekDays: CalendarioDiaDTO[] = [];

  constructor(
    private ordenCalendarioService: OrdenCalendarioService,
    private cd: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCalendario();
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
  }

  goToScheduleDay(dateStr: string): void {
    this.router.navigate(['/ordenes-calendario', dateStr, 'new']);
  }

  getLoadColor(count: number): string {
    if (count === 0) return 'bg-gray-100 text-gray-400';
    if (count <= 3) return 'bg-green-100 text-green-700 border-green-200';
    if (count <= 6) return 'bg-yellow-100 text-yellow-700 border-yellow-200';
    return 'bg-red-100 text-red-700 border-red-200';
  }

  // Safe date creation (prevents timezone shift)
  formatDayName(dateStr: string): string {
    const [year, month, day] = dateStr.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    return date.toLocaleDateString('es-NI', { weekday: 'short' }).toUpperCase();
  }

  formatFullDate(dateStr: string): string {
    const [year, month, day] = dateStr.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    return date.toLocaleDateString('es-NI', { day: 'numeric', month: 'short' });
  }

  formatTime(hora: number, minuto: number): string {
    return `${hora.toString().padStart(2, '0')}:${minuto.toString().padStart(2, '0')}`;
  }
}