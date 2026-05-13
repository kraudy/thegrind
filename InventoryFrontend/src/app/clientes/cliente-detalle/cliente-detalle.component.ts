import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ClienteService } from '../cliente.service';
import { Cliente } from '../cliente.model';

import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-cliente-detalle',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './cliente-detalle.html',
  styleUrls: ['./cliente-detalle.css'],
})
export class ClienteDetalleComponent implements OnInit {
  cliente: Cliente | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private clienteService: ClienteService,
    private cd: ChangeDetectorRef,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) return;

    const id = Number(idParam);
    this.clienteService.getById(id).subscribe({
      next: (cli) => {
        this.cliente = cli;
        this.cd.detectChanges();
      },
      error: (err) => console.error('[ClienteDetalle] Error cargando cliente', err),
    });
  }

  deleteCliente(): void {
    if (!this.cliente?.id) return;
    const nombreCompleto = `${this.cliente.nombre} ${this.cliente.apellido}`.trim();
    if (!confirm(`¿Seguro que deseas eliminar el cliente "${nombreCompleto}"?`)) return;

    const id = this.cliente.id;
    this.clienteService.delete(id).subscribe({
      next: () => {
        this.toastService.showToast('warning', 'Cliente eliminado', 'El cliente ha sido eliminado correctamente.', 4000);
        this.router.navigate(['/clientes']);
      },
      error: (err) => {
        console.error('Error eliminando cliente', err);
        this.toastService.showToast(
          'error',
          'Error al eliminar',
          err?.error?.message || 'No se pudo eliminar el cliente.',
          6000
        );
      },
    });
  }
}
