import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { OrdenCostoService } from '../orden-costo.service';
import { ToastService } from '../../shared/toast/toast.service';

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
    private ordenCostoService: OrdenCostoService,
    private toastService: ToastService
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
      this.toastService.showToast(
        'error',
        'Datos incompletos',
        'Debe seleccionar tipo de costo y trabajador para pagar.',
        6000
      );
      return;
    }

    if (confirm(`¿Está seguro de pagar ${this.totalMonto} al trabajador ${this.filters.trabajador || 'seleccionado'}?`)) {
      this.ordenCostoService.pagar(
        this.filters.tipoCosto,
        this.filters.trabajador,
        this.getQueryFilters()
      ).subscribe({
        next: () => {
          const trabajador = this.filters.trabajador || 'trabajador seleccionado';
          const monto = new Intl.NumberFormat('es-NI', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
          }).format(this.totalMonto);

          this.toastService.showToast(
            'success',
            'Pago confirmado',
            `Se pagaron ${monto} a ${trabajador}.`,
            4000
          );
          this.navigateToList();
        },
        error: (err) => {
          console.error(err);
          this.toastService.showToast(
            'error',
            'Error al procesar pago',
            err?.error?.message || 'No se pudo procesar el pago.',
            7000
          );
        }
      });
    }
  }

  cancelar(): void {
    this.navigateToList();
  }

  generarReciboPDF() {
    this.ordenCostoService
      .generarReciboPDF(this.filters.tipoCosto, this.filters.trabajador, this.getQueryFilters())
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `Recibo-${this.filters.trabajador}-${new Date().toISOString().slice(0,10)}.pdf`;
          a.click();
          window.URL.revokeObjectURL(url);
        },
        error: (err) => {
          console.error(err);
          alert('❌ Error al generar el recibo PDF');
        }
      });
  }
  
}