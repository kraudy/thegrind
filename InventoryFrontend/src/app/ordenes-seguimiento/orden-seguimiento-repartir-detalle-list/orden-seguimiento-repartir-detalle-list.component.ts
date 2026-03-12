import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { OrdenSeguimientoDetalle } from '../orden-seguimiento-detalle.model';
import { ProductoTipoEstado } from '../../productos-tipo-estados/producto-tipo-estado.model';
import { Usuario } from '../../usuarios/usuario.model';
import { UsuarioService } from '../../usuarios/usuario.service';

@Component({
  selector: 'app-orden-seguimiento-repartir-detalle-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orden-seguimiento-repartir-detalle-list.html',
  styleUrls: ['./orden-seguimiento-repartir-detalle-list.css'],
})
export class OrdenSeguimientoRepartirDetalleListComponent implements OnInit {
  idOrden!: number;
  detalles: OrdenSeguimientoDetalle[] = [];
  clienteNombre = 'Cargando...';

  possibleStatesMap = new Map<number, ProductoTipoEstado[]>();
  historyMap = new Map<number, any[]>();
  currentStateMap = new Map<number, string>();
  isOrderCompleted = false;

  reparadores: Usuario[] = [];
  normales: Usuario[] = [];
  selectedReparador: Record<number, string> = {};
  selectedNormal: Record<number, string> = {};
  assignedReparador = new Map<number, string>();
  assignedNormal = new Map<number, string>();
  assigning = new Set<number>();
  assignError: Record<number, string> = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: OrdenSeguimientoService,
    private usuarioService: UsuarioService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.idOrden = Number(this.route.snapshot.paramMap.get('idOrden'));
    this.clienteNombre = String(this.route.snapshot.paramMap.get('clienteNombre'));

    this.usuarioService.getReparadores().subscribe({
      next: (users) => (this.reparadores = users || []),
      error: (err) => console.error('❌ Error cargando reparadores', err),
    });

    this.usuarioService.getNormales().subscribe({
      next: (users) => (this.normales = users || []),
      error: (err) => console.error('❌ Error cargando usuarios normales', err),
    });

    this.load();
  }



  load() {
    this.service.getOrdenDetalleParaRepartir(this.idOrden).subscribe({
      next: (data) => {
        this.detalles = data || [];
        this.assigning.clear();
        this.assignError = {};
        this.loadStepperDataForAll();
        this.loadReparadoresAsignados();
        this.cd.detectChanges();
      },
      error: (err) => console.error('❌ Error cargando detalle de repartir', err),
    });
  }

  private loadReparadoresAsignados() {
    this.assignedReparador.clear();
    this.assignedNormal.clear();
    this.detalles.forEach((det) => {
      this.service.getReparador(det.idOrden, det.idOrdenDetalle).subscribe({
        next: (resp) => {
          if (resp?.usuario) {
            this.assignedReparador.set(det.idOrdenDetalle, resp.usuario);
          }
          this.cd.detectChanges();
        },
        error: (err) => console.error('❌ Error cargando reparador asignado', err),
      });

      this.service.getNormal(det.idOrden, det.idOrdenDetalle).subscribe({
        next: (resp) => {
          if (resp?.usuario) {
            this.assignedNormal.set(det.idOrdenDetalle, resp.usuario);
          }
          this.cd.detectChanges();
        },
        error: (err) => console.error('❌ Error cargando usuario normal asignado', err),
      });
    });
  }

  private loadStepperDataForAll() {
    this.possibleStatesMap.clear();
    this.historyMap.clear();
    this.currentStateMap.clear();

    this.detalles.forEach((det) => {
      const detId = det.idOrdenDetalle;
      this.currentStateMap.set(detId, det.estadoActual || '');

      this.service.getPossibleStates(det.tipoProducto, det.subTipoProducto).subscribe((states) => {
        this.possibleStatesMap.set(detId, states || []);
        this.cd.detectChanges();
      });

      this.service.getByDetalle(det.idOrden, det.idOrdenDetalle).subscribe((hist) => {
        this.historyMap.set(detId, hist || []);
        this.cd.detectChanges();
      });
    });

    // If no details are returned, the order is completed (all items moved to next stage)
    if (this.detalles.length === 0 && !this.isOrderCompleted) {
      this.isOrderCompleted = true;
    }
  }

  advanceDetail(det: OrdenSeguimientoDetalle) {
    this.service.advance(det.idOrden, det.idOrdenDetalle).subscribe(() => {
      this.load();
    });
  }

  assignReparador(det: OrdenSeguimientoDetalle) {
    const reparador = this.selectedReparador[det.idOrdenDetalle];
    if (!reparador) return;

    this.assignError[det.idOrdenDetalle] = '';
    this.assigning.add(det.idOrdenDetalle);

    this.service.assignTrabajo(det.idOrden, det.idOrdenDetalle, reparador).subscribe({
      next: () => {
        this.assignedReparador.set(det.idOrdenDetalle, reparador);
        this.service.advance(det.idOrden, det.idOrdenDetalle).subscribe({
          next: () => this.load(),
          error: (err) => {
            console.error('❌ Error avanzando tras asignar', err);
            this.assignError[det.idOrdenDetalle] = 'No se pudo avanzar al estado Reparación';
            this.assigning.delete(det.idOrdenDetalle);
          },
        });
      },
      error: (err) => {
        console.error('❌ Error asignando reparador', err);
        this.assignError[det.idOrdenDetalle] = 'No se pudo asignar el reparador';
        this.assigning.delete(det.idOrdenDetalle);
      },
    });
  }

  assignNormal(det: OrdenSeguimientoDetalle) {
    const usuario = this.selectedNormal[det.idOrdenDetalle];
    if (!usuario) return;

    this.assignError[det.idOrdenDetalle] = '';
    this.assigning.add(det.idOrdenDetalle);

    // Ambos usan el mismo servicio
    this.service.assignTrabajo(det.idOrden, det.idOrdenDetalle, usuario).subscribe({
      next: () => {
        this.assignedNormal.set(det.idOrdenDetalle, usuario);
        this.service.advance(det.idOrden, det.idOrdenDetalle).subscribe({
          next: () => this.load(),
          error: (err) => {
            console.error('❌ Error avanzando tras asignar normal', err);
            this.assignError[det.idOrdenDetalle] = 'No se pudo avanzar al siguiente estado';
            this.assigning.delete(det.idOrdenDetalle);
          },
        });
      },
      error: (err) => {
        console.error('❌ Error asignando usuario normal', err);
        this.assignError[det.idOrdenDetalle] = 'No se pudo asignar el usuario';
        this.assigning.delete(det.idOrdenDetalle);
      },
    });
  }

  getPossibleStates(detId: number): ProductoTipoEstado[] {
    return this.possibleStatesMap.get(detId) || [];
  }

  isReached(detId: number, estado: string): boolean {
    if (!estado) return false;
    return this.historyMap.get(detId)?.some((h: any) => h.estado === estado) || false;
  }

  isCurrent(detId: number, estado: string | undefined): boolean {
    if (!estado) return false;
    return this.currentStateMap.get(detId) === estado;
  }

  isLastState(detId: number): boolean {
    const states = this.getPossibleStates(detId);
    if (states.length === 0) return true;
    return this.isCurrent(detId, states[states.length - 1]?.estado);
  }

  getAssignedReparador(detId: number): string | undefined {
    return this.assignedReparador.get(detId);
  }

  getAssignedNormal(detId: number): string | undefined {
    return this.assignedNormal.get(detId);
  }

  isAssigning(detId: number): boolean {
    return this.assigning.has(detId);
  }

  goBack() {
    this.router.navigate(['/ordenes-seguimiento-repartir']);
  }
}
