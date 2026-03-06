import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { OrdenSeguimientoService } from '../orden-seguimiento.service';
import { OrdenSeguimiento } from '../orden-seguimiento.model';

@Component({
  selector: 'app-orden-seguimiento-entrega-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './orden-seguimiento-entrega-list.html',
  styleUrls: ['./orden-seguimiento-entrega-list.css'],
})
export class OrdenSeguimientoEntregaListComponent implements OnInit {
  ordenes: OrdenSeguimiento[] = [];

  constructor(
    private ordenSeguimientoService: OrdenSeguimientoService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadOrdenes();
  }

  loadOrdenes(): void {
    this.ordenSeguimientoService.getOrdenesParaEntrega().subscribe({
      next: (ordenes) => {
        this.ordenes = ordenes;
        this.cd.detectChanges();
      },
      error: (error) => {
        console.error('Error loading ordenes para entrega:', error);
      }
    });
  }

  viewDetails(idOrden: number): void {
    this.router.navigate(['/ordenes-seguimiento', idOrden]);
  }
}
