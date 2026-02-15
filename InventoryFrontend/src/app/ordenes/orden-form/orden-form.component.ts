import { Component, Input, Output, EventEmitter, OnChanges, OnInit, ChangeDetectorRef } from '@angular/core';

import { CommonModule, Location  } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { OrdenService } from '../orden.service';
import { Orden } from '../orden.model';

import { ClienteService } from '../../clientes/cliente.service';
import { Cliente } from '../../clientes/cliente.model';

import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-orden-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orden-form.html',
  styleUrls: ['./orden-form.css'],
})
export class OrdenFormComponent implements OnChanges, OnInit{
  @Input() orden: Orden | null = null;
  @Output() ordenSaved = new EventEmitter<void>();

  isEdit = false;
  ordenId: number | null = null;

  //Autocomplete properties
  filteredClientes: Cliente[] = [];
  selectedCliente: Cliente | null = null;
  searchTerm: string = '';
  private searchTerms = new Subject<string>();
  
  constructor(
    private ordenService: OrdenService,
    private clienteService: ClienteService,
    private location: Location,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef
  ) {}

  formOrden: Partial<Orden> = { 
    idCliente: 0,
    creadaPor: '',
    totalMonto: 0,
    totalProductos: 0,
    fechaEntrega: null,
    fechaPreparada: null,
    fechaDespachada: null
  };

  ngOnInit(): void {
    if (!this.orden) {
      this.resetForm();
      this.isEdit = false;
    }

    //Debounced search setup
    this.searchTerms.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => term ? this.clienteService.search(term) : of([]))
    ).subscribe(clientes => {
      this.filteredClientes = clientes;
      this.cd.detectChanges();
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) { return; }

    const id = Number(idParam);
    if (isNaN(id)) { return; }

    this.ordenId = id;
    this.ordenService.getById(id).subscribe({
      next: (data) => {
        this.loadForm(data);
        this.isEdit = true;
        console.log('[OrdenForm] loaded orden for edit', data);
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[OrdenForm] failed to load orden', err);
        this.resetForm();
        this.isEdit = false;
        this.cd.detectChanges();
      }
    });
  }

  ngOnChanges(): void {
    if (this.orden) {
      this.loadForm(this.orden);
      this.isEdit = !!this.orden.id;
      if (this.isEdit && this.orden.idCliente) {
        this.loadClienteForDisplay();
      }
    } else {
      this.resetForm();
      this.isEdit = false;
    }
  }

  // Load full client for display in edit mode
  private loadClienteForDisplay(): void {
    const id = this.formOrden.idCliente;
    if (!id || id <= 0) return;

    this.clienteService.getById(id).subscribe({
      next: (cli) => {
        this.selectedCliente = cli;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load cliente for display', err);
        this.selectedCliente = null;
        this.cd.detectChanges();
      }
    });
  }

  private loadForm(data: Orden): void {
    this.formOrden = { ...data }; // Spread the full Orden — includes real dates on edit
  }

  goBack(): void {
    this.location.back();
  }

  resetForm(): void {
    this.formOrden = {
      idCliente: 0,
      creadaPor: '',
      totalMonto: 0,
      totalProductos: 0,
      fechaEntrega: null,
      fechaPreparada: null,
      fechaDespachada: null
    };

    // Reset autocomplete state
    this.searchTerm = '';
    this.selectedCliente = null;
    this.filteredClientes = [];
  }

  //Search handling
  onSearchChange(term: string): void {
    if (this.isEdit) return;

    this.searchTerm = term;

    if (!term.trim()) {
      this.selectedCliente = null;
      this.formOrden.idCliente = 0;
      this.filteredClientes = [];
    }
    this.searchTerms.next(term.trim());
  }

  //Select cliente
  selectCliente(cliente: Cliente): void {
    if (this.isEdit) return;

    this.selectedCliente = cliente;
    this.formOrden.idCliente = Number(cliente.id);
    this.searchTerm = `${cliente.nombre} ${cliente.apellido || ''} (ID: ${cliente.id})`;
    this.filteredClientes = [];
    this.cd.detectChanges();
  }


  onSubmit(): void {
    const payload: Partial<Orden> = {
      idCliente: this.formOrden.idCliente,
      creadaPor: this.formOrden.creadaPor ?? '',
      totalMonto: this.formOrden.totalMonto ?? 0,
      totalProductos: this.formOrden.totalProductos ?? 0,
      fechaEntrega: this.formOrden.fechaEntrega,
      fechaPreparada: this.formOrden.fechaPreparada ?? null,
      fechaDespachada: this.formOrden.fechaDespachada ?? null
    };

    if (this.isEdit && this.formOrden.id) {
      // Update Orden and add id
      (payload).id = this.formOrden.id;

      this.ordenService.update(this.formOrden.id, payload).subscribe({
        next: () => {
          this.ordenSaved.emit();
          this.goBack();
        },
        complete: () => {
          console.log('Orden payload send sucessfully for update:', payload);
        },
        error: (err) => {
          console.error('Failed to update orden', err);
          alert('Error updating orden: ' + (err?.message || 'Unknown error'));
        }
      });
    } else {
      // Create new Orden
      this.ordenService.create(payload).subscribe({
        next: () => {
          this.ordenSaved.emit();
          this.resetForm();
          this.goBack();
        },
        complete: () => {
          console.log('Orden payload send sucessfully for create:', payload);
        },
        error: (err) => {
          console.error('Failed to create orden', err);
          alert('Error creating orden: ' + (err?.message || 'Unknown error'));
        }
      });
    }
  }

}
