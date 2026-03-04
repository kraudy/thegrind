import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { OrdenSeguimiento } from '../orden-seguimiento.model';

@Component({
  selector: 'app-orden-seguimiento-preparacion-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './orden-seguimiento-preparacion-list.html',
  styleUrls: ['./orden-seguimiento-preparacion-list.css'],
})
export class OrdenSeguimientoPreparacionListComponent implements OnInit {
  ordenes: OrdenSeguimiento[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private ordenSeguimientoService: OrdenSeguimientoService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadOrdenesParaPreparacion();
  }

  loadOrdenesParaPreparacion(): void {
    this.loading = true;
    this.errorMessage = '';

    this.ordenSeguimientoService.getOrdenesParaPreparacion().subscribe({
      next: (data) => {
        this.ordenes = data || [];
        this.loading = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[OrdenSeguimientoPreparacionList] error loading data', err);
        this.ordenes = [];
        this.loading = false;
        this.errorMessage = 'No se pudo cargar la lista de órdenes para preparación.';
        this.cd.detectChanges();
      },
    });
  }

  goToDetalle(orden: OrdenSeguimiento): void {
    this.router.navigate(['/ordenes-seguimiento-preparacion', orden.id, orden.clienteNombre]);
  }

  getTiempoRestanteClass(tiempoRestante: string): string {
    const t = (tiempoRestante || '').toLowerCase();
    if (t.includes('-')) return 'text-red-600';
    if (t.includes('00:')) return 'text-amber-600';
    return 'text-emerald-600';
  }
}
