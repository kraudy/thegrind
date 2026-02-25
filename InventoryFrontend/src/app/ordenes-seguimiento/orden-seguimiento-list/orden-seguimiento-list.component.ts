import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { OrdenSeguimiento } from '../orden-seguimiento.model';
import { ProductoTipoEstado } from '../../productos-tipo-estados/producto-tipo-estado.model';

@Component({
  selector: 'app-orden-seguimiento-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './orden-seguimiento-list.html',
  styleUrls: ['./orden-seguimiento-list.css'],
})
export class OrdenSeguimientoListComponent implements OnInit {
  idOrden!: number;
  detalles: OrdenSeguimiento[] = [];
  clienteNombre = 'Cargando...';

  possibleStatesMap = new Map<number, ProductoTipoEstado[]>();
  historyMap = new Map<number, any[]>();
  currentStateMap = new Map<number, string>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: OrdenSeguimientoService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.idOrden = Number(this.route.snapshot.paramMap.get('idOrden'));
    this.clienteNombre = String(this.route.snapshot.paramMap.get('clienteNombre'));
    this.load();
  }

  load() {
    //TODO: Here call getPossibleStates and set it to a local map to just key it from getPossibleStatesJ()
    // to not be calling the backend for each state
    // The keys needs to be tipo and subtipo to not repeat the same pattern multiple times
    this.service.getFullByOrden(this.idOrden).subscribe({
      next: (data) => {
        this.detalles = data;
        this.loadStepperDataForAll();

        this.cd.detectChanges();
      },
      error: (err) => console.error('❌ Error cargando seguimiento', err)
    });
  }

  private loadStepperDataForAll() {
    this.possibleStatesMap.clear();
    this.historyMap.clear();
    this.currentStateMap.clear();

    this.detalles.forEach(det => {
      const detId = det.idOrdenDetalle;
      this.currentStateMap.set(detId, det.estadoActual || '');

      this.service.getPossibleStates(det.tipoProducto, det.subTipoProducto).subscribe(states => {
        this.possibleStatesMap.set(detId, states);
        this.cd.detectChanges();
      });

      this.service.getByDetalle(det.idOrden, det.idOrdenDetalle).subscribe(hist => {
        this.historyMap.set(detId, hist);
        this.cd.detectChanges();
      });
    });
  }

  advanceDetail(det: OrdenSeguimiento) {
    this.service.advance(det.idOrden, det.idOrdenDetalle).subscribe(() => {
      this.load();
    });
  }

  getPossibleStates(detId: number): ProductoTipoEstado[] {
    return this.possibleStatesMap.get(detId) || [];
  }

  isReached(detId: number, estado: string): boolean {
    return this.historyMap.get(detId)?.some((h: any) => h.estado === estado) || false;
  }

  isCurrent(detId: number, estado: string | undefined): boolean {
    if (!estado) return false;
    return this.currentStateMap.get(detId) === estado;
  }

  // Nuevo helper seguro para el botón
  isLastState(detId: number): boolean {
    const states = this.getPossibleStates(detId);
    if (states.length === 0) return true;
    return this.isCurrent(detId, states[states.length - 1].estado);
  }

  goBack() {
    this.router.navigate(['/ordenes-calendario']);
  }
}