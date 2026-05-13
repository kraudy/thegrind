import { Component, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { ClienteService } from '../cliente.service';
import { Cliente } from '../cliente.model';

@Component({
  selector: 'app-cliente-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './cliente-list.html',
  styleUrls: ['./cliente-list.css'],
})
export class ClienteListComponent implements OnInit {
  clientes: Cliente[] = [];

  // Filters
  searchTerm = '';      // id or nombre/apellido
  searchTelefono = '';
  searchCorreo = '';

  private readonly FILTERS_KEY = 'clienteList.filters';

  constructor(
    private clienteService: ClienteService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.restoreFilters();
    this.loadClientes();
  }

  private restoreFilters(): void {
    try {
      const raw = sessionStorage.getItem(this.FILTERS_KEY);
      if (!raw) return;
      const f = JSON.parse(raw);
      this.searchTerm = f.searchTerm ?? '';
      this.searchTelefono = f.searchTelefono ?? '';
      this.searchCorreo = f.searchCorreo ?? '';
    } catch {
      /* ignore corrupted state */
    }
  }

  private persistFilters(): void {
    try {
      sessionStorage.setItem(this.FILTERS_KEY, JSON.stringify({
        searchTerm: this.searchTerm,
        searchTelefono: this.searchTelefono,
        searchCorreo: this.searchCorreo,
      }));
    } catch {
      /* ignore quota errors */
    }
  }

  loadClientes(): void {
    // Intelligent search: numeric term → id, else → nombre/apellido
    let filterId: number | undefined;
    let filterNombre: string | undefined;

    const term = this.searchTerm.trim();
    if (term) {
      const num = Number(term);
      if (!isNaN(num) && num > 0 && Number.isInteger(num)) {
        filterId = num;
      } else {
        filterNombre = term;
      }
    }

    this.clienteService.getAllWithFilters({
      id: filterId,
      nombre: filterNombre,
      telefono: this.searchTelefono.trim() || undefined,
      correo: this.searchCorreo.trim() || undefined,
    }).subscribe({
      next: (data) => {
        this.clientes = data || [];
        this.persistFilters();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('[ClienteList] failed to load clientes', err);
        this.clientes = [];
        if (err?.status === 0) {
          console.log('Cannot reach backend (network error). Is the Spring server running?');
        } else if (err?.status) {
          console.log(`Backend error ${err.status}: ${err?.message || err?.statusText || 'unknown'}`);
        } else {
          console.log('Unexpected error loading clientes (check console)');
        }
        this.cdr.detectChanges();
      }
    });
  }

  onFilterChange(): void {
    this.loadClientes();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.searchTelefono = '';
    this.searchCorreo = '';
    try { sessionStorage.removeItem(this.FILTERS_KEY); } catch { /* ignore */ }
    this.loadClientes();
  }

  deleteCliente(id?: number): void {
    if (id && confirm('¿Seguro desea eliminar?')){
      this.clienteService.delete(id).subscribe(() => this.loadClientes());
    }
  }

  viewDetails(id: number): void {
    this.router.navigate(['/clientes-detalle', id]);
  }

}
