import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

import { FacturaDetalle } from '../factura-detalle.model';
import { FacturaDetalleService } from '../factura-detalle.service';

@Component({
	selector: 'app-factura-detalle-list',
	standalone: true,
	imports: [CommonModule],
	templateUrl: './factura-detalle-list.html',
	styleUrls: ['./factura-detalle-list.css'],
})
export class FacturaDetalleListComponent implements OnInit {
	idFactura = 0;
	clienteNombre = 'Cliente no especificado';

	idOrden: number | null = null;
	estado = '';
	usuarioCreacion = '';
	fechaCreacion: string | null = null;

	detalles: FacturaDetalle[] = [];

	loading = false;
	errorMessage = '';

	totalCantidad = 0;
	totalSubtotal = 0;
	totalFactura = 0;

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		private facturaDetalleService: FacturaDetalleService,
		private cd: ChangeDetectorRef
	) {}

	ngOnInit(): void {
		const rawId = this.route.snapshot.paramMap.get('idFactura');
		const parsedId = rawId ? Number(rawId) : NaN;

		if (!rawId || Number.isNaN(parsedId) || parsedId <= 0) {
			this.errorMessage = 'Factura invalida. No se pudo cargar el detalle.';
			return;
		}

		this.idFactura = parsedId;
		this.clienteNombre =
			this.route.snapshot.paramMap.get('clienteNombre') ||
			this.route.snapshot.queryParamMap.get('clienteNombre') ||
			'Cliente no especificado';

		this.idOrden = this.parseOptionalNumber(this.route.snapshot.queryParamMap.get('idOrden'));
		this.estado = this.route.snapshot.queryParamMap.get('estado') || '';
		this.usuarioCreacion = this.route.snapshot.queryParamMap.get('usuarioCreacion') || '';
		this.fechaCreacion = this.route.snapshot.queryParamMap.get('fechaCreacion');

		const totalFromList = this.parseOptionalNumber(
			this.route.snapshot.queryParamMap.get('totalMontoFactura') ||
			this.route.snapshot.queryParamMap.get('totalMontoOrden')
		);

		if (totalFromList !== null) {
			this.totalFactura = totalFromList;
		}

		this.loadDetalles();
	}

	private parseOptionalNumber(value: string | null): number | null {
		if (value === null || value.trim() === '') {
			return null;
		}

		const parsed = Number(value);
		return Number.isNaN(parsed) ? null : parsed;
	}

	private loadDetalles(): void {
		this.loading = true;
		this.errorMessage = '';

		this.facturaDetalleService.getByFactura(this.idFactura).subscribe({
			next: (data) => {
				this.detalles = data || [];

				this.totalCantidad = this.detalles.reduce((acc, item) => acc + (item.cantidad || 0), 0);
				this.totalSubtotal = this.detalles.reduce((acc, item) => acc + (item.subtotal || 0), 0);

				if (this.totalFactura <= 0) {
					this.totalFactura = this.totalSubtotal;
				}

				this.loading = false;
				this.cd.detectChanges();
			},
			error: (err: any) => {
				console.error('[FacturaDetalleList] failed to load detalles', err);
				this.detalles = [];
				this.loading = false;

				if (err?.status === 0) {
					this.errorMessage = 'No se puede conectar con el backend. Verifica que Spring este activo.';
				} else if (err?.status) {
					this.errorMessage = `Error ${err.status}: ${err?.message || err?.statusText || 'desconocido'}`;
				} else {
					this.errorMessage = 'Error inesperado al cargar el detalle de la factura.';
				}

				this.cd.detectChanges();
			},
		});
	}

	goBack(): void {
		this.router.navigate(['/facturas']);
	}
}
