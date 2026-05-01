import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { OrdenCostoService } from '../orden-costo.service';
import { OrdenCosto } from '../orden-costo.model';
import { UsuarioService } from '../../usuarios/usuario.service';
import { UsuarioNombre } from '../../usuarios/usuario-nombre.model';

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
  loadingTrabajadores = false;
  errorMessage = '';
  trabajadoresDisponibles: string[] = [];
  usuarioActual = localStorage.getItem('usuario') || '';

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
    private usuarioService: UsuarioService,
    private route: ActivatedRoute,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const tipoCostoParam = this.route.snapshot.paramMap.get('tipoCosto');
    const trabajadorParam = this.route.snapshot.paramMap.get('trabajador');

    if (!tipoCostoParam || !trabajadorParam) {
      this.router.navigate(['/ordenes-costo/pagar', 'Reparacion', this.usuarioActual || 'admin'], { replaceUrl: true });
      return;
    }

    this.filters.tipoCosto = tipoCostoParam;
    this.filters.trabajador = trabajadorParam;
    this.loadTrabajadoresByTipo(this.filters.tipoCosto, true);
  }

  private loadTrabajadoresByTipo(tipoCosto: string, preserveCurrent: boolean): void {
    this.trabajadoresDisponibles = [];

    if (!tipoCosto) {
      if (!preserveCurrent) {
        this.filters.trabajador = '';
      }
      return;
    }

    let request$;
    if (tipoCosto === 'Pegado') {
      request$ = this.usuarioService.getPegadoresNombres();
    } else if (tipoCosto === 'Reparacion') {
      request$ = this.usuarioService.getReparadoresNombre();
    } else {
      if (!preserveCurrent) {
        this.filters.trabajador = '';
      }
      return;
    }

    this.loadingTrabajadores = true;

    request$.subscribe({
      next: (usuarios: UsuarioNombre[]) => {
        this.trabajadoresDisponibles = (usuarios || [])
          .map(u => u.usuario)
          .filter(u => !!u && u !== this.usuarioActual);

        if (preserveCurrent) {
          if (!this.filters.trabajador || !this.trabajadoresDisponibles.includes(this.filters.trabajador)) {
            this.filters.trabajador = this.trabajadoresDisponibles[0] || '';

            if (this.filters.trabajador) {
              this.router.navigate(['/ordenes-costo/pagar', this.filters.tipoCosto, this.filters.trabajador], {
                replaceUrl: true
              });
            }
          }
        } else {
          this.filters.trabajador = this.trabajadoresDisponibles.length === 1 ? this.trabajadoresDisponibles[0] : '';
        }

        this.loadOrdenes();

        this.loadingTrabajadores = false;
        this.cd.detectChanges();
      },
      error: (err: any) => {
        console.error(err);
        this.loadingTrabajadores = false;
        this.trabajadoresDisponibles = [];

        if (!preserveCurrent) {
          this.filters.trabajador = '';
          this.loadOrdenes();
        }

        this.errorMessage = 'No se pudo cargar la lista de trabajadores.';
        this.cd.detectChanges();
      }
    });
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

  onTipoCostoChange(tipoCosto: string): void {
    this.filters.tipoCosto = tipoCosto;
    this.errorMessage = '';
    this.loadTrabajadoresByTipo(tipoCosto, false);
  }

  onTrabajadorChange(): void {
    this.loadOrdenes();
  }

  clearFilters(): void {
    this.filters = {
      tipoCosto: this.route.snapshot.paramMap.get('tipoCosto') || '',
      trabajador: this.route.snapshot.paramMap.get('trabajador') || '',
      fechaInicio: '',
      fechaFin: '',
      idOrden: undefined,
      idOrdenDetalle: undefined,
      pagado: false
    };
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