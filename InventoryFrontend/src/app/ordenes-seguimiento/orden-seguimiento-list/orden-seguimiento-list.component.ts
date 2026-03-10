import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { OrdenCalendarioService } from '../../ordenes-calendario/orden-calendario.service';
import { OrdenSeguimientoDetalle } from '../orden-seguimiento-detalle.model';
import { ProductoTipoEstado } from '../../productos-tipo-estados/producto-tipo-estado.model';
import { EstadosPorDetalleDTO } from '../estados-por-detalle.model';

@Component({
  selector: 'app-orden-seguimiento-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './orden-seguimiento-list.html',
  styleUrls: ['./orden-seguimiento-list.css'],
})
export class OrdenSeguimientoListComponent implements OnInit {
  idOrden!: number;
  detalles: OrdenSeguimientoDetalle[] = [];
  clienteNombre = 'Cargando...';

  possibleStatesMap = new Map<number, string[]>();
  currentStateMap = new Map<number, string>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ordenSeguimientoService: OrdenSeguimientoService,
    private ordenCalendarioService: OrdenCalendarioService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.idOrden = Number(this.route.snapshot.paramMap.get('idOrden'));
    this.clienteNombre = String(this.route.snapshot.paramMap.get('clienteNombre'));
    this.load();
  }

  load() {
    this.ordenSeguimientoService.getFullByOrden(this.idOrden).subscribe({
      next: (data) => {
        this.detalles = data;
        this.loadEstadosPorDetalle();
        this.cd.detectChanges();
      },
      error: (err) => console.error('❌ Error cargando seguimiento', err)
    });
  }

  private loadEstadosPorDetalle() {
    this.ordenSeguimientoService.getEstadosPorDetalle(this.idOrden).subscribe({
      next: (data) => {
        data.forEach(item => {
          this.possibleStatesMap.set(item.idOrdenDetalle, item.estados);
          this.currentStateMap.set(item.idOrdenDetalle, item.estadoActual);
        });
        this.cd.detectChanges();
      },
      error: (err) => console.error('❌ Error cargando estados por detalle', err)
    });
  }

  reverseDetail(det: OrdenSeguimientoDetalle) {
    this.ordenSeguimientoService.reverse(det.idOrden, det.idOrdenDetalle).subscribe({
      next: () => {
        this.load();
      },
      error: (error) => {
        alert(error.error?.message || 'Error al regresar el estado');
      }
    });
  }

  getPossibleStates(detId: number): string[] {
    return this.possibleStatesMap.get(detId) || [];
  }

  isReached(detId: number, estado: string): boolean {
    return this.possibleStatesMap.get(detId)?.some((h: any) => h.estado === estado) || false;
  }

  isCurrent(detId: number, estado: string | undefined): boolean {
    if (!estado) return false;
    return this.currentStateMap.get(detId) === estado;
  }

  // Nuevo helper seguro para el botón
  isFirstState(detId: number): boolean {
    const states = this.getPossibleStates(detId);
    if (states.length === 0) return true;
    return this.isCurrent(detId, states[0]);
  }

  goBack() {
    this.router.navigate(['/ordenes-calendario']);
  }

  deleteOrderFromCalendar(): void {
    if (confirm(`¿Estás seguro de que quieres eliminar la orden #${this.idOrden} del calendario?`)) {
      this.ordenCalendarioService.delete(this.idOrden).subscribe({
        next: () => {
          alert('Orden eliminada del calendario exitosamente');
          this.router.navigate(['/ordenes-calendario']); // Regresar al calendario después de eliminar
        },
        error: (err) => {
          console.error('Error eliminando orden del calendario', err);
          if (err.status === 400) {
            alert('No se puede eliminar la orden del calendario porque algunos detalles ya han avanzado en su proceso de producción');
          } else {
            alert('Error al eliminar la orden del calendario');
          }
        }
      });
    }
  }
}