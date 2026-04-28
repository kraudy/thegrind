import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { OrdenCostoService } from '../orden-costo.service';
import { OrdenCosto } from '../orden-costo.model';

@Component({
  selector: 'app-orden-costo-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './orden-costo-list.html',
  styleUrls: ['./orden-costo-list.css']
})
export class OrdenCostoListComponent implements OnInit {
  ordenesCosto: OrdenCosto[] = [];
  totalMonto: number = 0;
  loading = false;
  errorMessage = '';

  filters: any = {
    tipoCosto: '',
    trabajador: '',
    fechaInicio: '',
    fechaFin: '',
    idOrden: undefined,
    idOrdenDetalle: undefined,
    pagado: false
  };

  constructor(
    private ordenCostoService: OrdenCostoService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadOrdenes();
  }

  private hasRequiredPathParams(): boolean {
    return !!this.filters.tipoCosto && !!this.filters.trabajador;
  }

  private getQueryFilters() {
    return {
      fechaInicio: this.filters.fechaInicio || undefined,
      fechaFin: this.filters.fechaFin || undefined,
      idOrden: this.filters.idOrden,
      idOrdenDetalle: this.filters.idOrdenDetalle,
      pagado: this.filters.pagado
    };
  }

  loadOrdenes(): void {
    if (!this.hasRequiredPathParams()) {
      this.ordenesCosto = [];
      this.totalMonto = 0;
      this.loading = false;
      this.errorMessage = 'Selecciona tipo de costo y trabajador para consultar.';
      this.cd.detectChanges();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    const queryFilters = this.getQueryFilters();

    this.ordenCostoService.getAll(this.filters.tipoCosto, this.filters.trabajador, queryFilters).subscribe({
      next: (data) => {
        this.ordenesCosto = data || [];
        this.loadTotal();
        this.loading = false;
        this.cd.detectChanges();
      },
      error: (err: any) => {
        console.error(err);
        this.ordenesCosto = [];
        this.totalMonto = 0;
        this.loading = false;
        this.errorMessage = 'No se pudo cargar los costos.';
        this.cd.detectChanges();
      }
    });
  }

  private loadTotal(): void {
    if (!this.hasRequiredPathParams()) {
      this.totalMonto = 0;
      return;
    }

    const queryFilters = this.getQueryFilters();
    this.ordenCostoService.getTotal(this.filters.tipoCosto, this.filters.trabajador, queryFilters).subscribe({
      next: (total) => {
        this.totalMonto = total;
        this.cd.detectChanges();
      },
      error: () => {
        this.totalMonto = 0;
      }
    });
  }

  onFilterChange(): void {
    this.loadOrdenes();
  }

  clearFilters(): void {
    this.filters = { tipoCosto: '', trabajador: '', fechaInicio: '', fechaFin: '', idOrden: undefined, idOrdenDetalle: undefined, pagado: false };
    this.loadOrdenes();
  }

  confirmarPago(): void {
    if (this.ordenesCosto.length === 0) return;
    this.router.navigate(['/ordenes-costo/pagar/confirmar'], {
      queryParams: {
        ...this.filters,
        totalMonto: this.totalMonto
      }
    });
  }
}