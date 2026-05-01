import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { OrdenCostoService } from '../orden-costo.service';

@Component({
  selector: 'app-orden-costo-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './orden-costo-form.html',
  styleUrls: ['./orden-costo-form.css']
})
export class OrdenCostoFormComponent implements OnInit {
  filters: any = {};
  totalMonto: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ordenCostoService: OrdenCostoService
  ) {}

  ngOnInit(): void {
    this.filters = {
      tipoCosto: this.route.snapshot.queryParamMap.get('tipoCosto') || undefined,
      trabajador: this.route.snapshot.queryParamMap.get('trabajador') || undefined,
      fechaInicio: this.route.snapshot.queryParamMap.get('fechaInicio') || undefined,
      fechaFin: this.route.snapshot.queryParamMap.get('fechaFin') || undefined,
      idOrden: this.route.snapshot.queryParamMap.get('idOrden') ? +this.route.snapshot.queryParamMap.get('idOrden')! : undefined,
      idOrdenDetalle: this.route.snapshot.queryParamMap.get('idOrdenDetalle') ? +this.route.snapshot.queryParamMap.get('idOrdenDetalle')! : undefined,
      pagado: false
    };

    this.totalMonto = this.route.snapshot.queryParamMap.get('totalMonto') 
      ? parseFloat(this.route.snapshot.queryParamMap.get('totalMonto')!) 
      : 0;
  }

  private hasRequiredPathParams(): boolean {
    return !!this.filters.tipoCosto && !!this.filters.trabajador;
  }

  private getQueryFilters() {
    return {
      fechaInicio: this.filters.fechaInicio,
      fechaFin: this.filters.fechaFin,
      idOrden: this.filters.idOrden,
      idOrdenDetalle: this.filters.idOrdenDetalle
    };
  }

  private navigateToList(): void {
    const tipoCosto = this.filters.tipoCosto || 'Reparacion';
    const trabajador = this.filters.trabajador || localStorage.getItem('usuario') || 'admin';
    this.router.navigate(['/ordenes-costo/pagar'], {
      queryParams: {
        tipoCosto,
        trabajador
      }
    });
  }

  confirmarPago(): void {
    if (this.totalMonto <= 0) return;

    if (!this.hasRequiredPathParams()) {
      alert('❌ Debe seleccionar tipo de costo y trabajador para pagar.');
      return;
    }

    if (confirm(`¿Está seguro de pagar ${this.totalMonto} unidades trabajadas al trabajador ${this.filters.trabajador || 'seleccionado'}?`)) {
      this.ordenCostoService.pagar(
        this.filters.tipoCosto,
        this.filters.trabajador,
        this.getQueryFilters()
      ).subscribe({
        next: () => {
          alert('✅ Costos pagados correctamente');
          this.navigateToList();
        },
        error: (err) => {
          console.error(err);
          alert('❌ Error al procesar el pago');
        }
      });
    }
  }

  cancelar(): void {
    this.navigateToList();
  }
}