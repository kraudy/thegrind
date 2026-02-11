import { Component, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ClienteService } from '../cliente.service';
import { Cliente } from '../cliente.model';

@Component({
  selector: 'app-cliente-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './cliente-list.html',
  styleUrls: ['./cliente-list.css'],
})
export class ClienteListComponent implements OnInit {
  clientes: Cliente[] = [];

  constructor(
    private clienteService: ClienteService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadClientes();
  }

  loadClientes(): void {
    this.clienteService.getAll().subscribe({
      next: (data) => {
        this.clientes = data || [];
        console.log('[ClienteList] loaded clientes:', this.clientes.length, this.clientes);
        this.cdr.detectChanges(); // force view update if zone isn't triggering it
      },
      error: (err) => {
        // log full error to inspect status/code in the console
        console.error('[ClienteList] failed to load clientes', err);
        this.clientes = [];
        if (err?.status === 0) {
          console.log('Cannot reach backend (network error). Is the Spring server running?');
        } else if (err?.status) {
          console.log(`Backend error ${err.status}: ${err?.message || err?.statusText || 'unknown'}`);
        } else {
          console.log('Unexpected error loading ordenes (check console)');
        }
      }
    });
  }

  deleteCliente(id?: number): void {
    if (id && confirm('¿Seguro desea eliminar?')){
      this.clienteService.delete(id).subscribe(() => this.loadClientes());
    }
  }

}
