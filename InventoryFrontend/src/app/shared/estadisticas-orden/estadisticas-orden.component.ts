import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrdenEstadisticas } from '../../ordenes-calendario/orden-estadisticas.model';

@Component({
  selector: 'app-estadisticas-orden',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './estadisticas-orden.component.html',
})
export class EstadisticasOrdenComponent {

  @Input() estadisticas: OrdenEstadisticas = {
    ordenesRecibidas: 0,
    reparadores: [],
    normales: [],
    repartidas: [],
    impresionNormal: 0,
    impresionReparacion: 0,
    bodega: 0,
    armado: 0,
    calado: 0,
    pegado: 0,
    enmarcado: 0,
    alistado: 0
  };

  // 🔥 Filters – you can turn sections on/off from any parent
  @Input() showRepartidorSection: boolean = true;
  @Input() showProcesoProduccionSection: boolean = true;
}