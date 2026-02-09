import { Component, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { OrdenService } from '../orden.service';
import { Orden } from '../orden.model';

@Component({
  selector: 'app-orden-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './orden-list.html',
  styleUrls: ['./orden-list.css'],
})
export class OrdenListComponent implements OnInit {
  ordenes: Orden[] = [];

  constructor(
      private ordenService: OrdenService,
      private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadOrdenes();
  }

  loadOrdenes(): void {
    this.ordenService.getAll().subscribe({
      next: (data) => {
        this.ordenes = data || [];
        console.log('[OrdenList] loaded ordenes:', this.ordenes.length, this.ordenes);
        this.cd.detectChanges(); // force view update if zone isn't triggering it
      },
      error: (err) => {
        // log full error to inspect status/code in the console
        console.error('[OrdenList] failed to load ordenes', err);
        this.ordenes = [];
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

  deleteOrden(id?: number): void {
    if (id && confirm('¿Seguro desea eliminar?')){
      this.ordenService.delete(id).subscribe(() => this.loadOrdenes());
    }
  }

}
