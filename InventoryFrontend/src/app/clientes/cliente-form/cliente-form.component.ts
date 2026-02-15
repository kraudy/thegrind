import { Component, Input, Output, EventEmitter, OnChanges, OnInit, ChangeDetectorRef } from '@angular/core';

import { CommonModule, Location  } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { ClienteService } from '../cliente.service';
import { Cliente } from '../cliente.model';

@Component({
  selector: 'app-cliente-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cliente-form.html',
  styleUrls: ['./cliente-form.css'],
})
export class ClienteFormComponent implements OnChanges, OnInit{
  @Input() cliente: Cliente | null = null;
  @Output() clienteSaved = new EventEmitter<void>();

  isEdit = false;
  clienteId: number | null = null;

  constructor(
    private clienteService: ClienteService,
    private location: Location,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef
  ) {}

  formCliente: Partial<Cliente> = {
    nombre: '',
    apellido: '',
    telefono: '',
    correo: '',
    direccion: ''
  };

  ngOnInit(): void {
    if (!this.cliente) {
      console.log('[ClienteForm] no cliente provided', this.cliente);
      this.resetForm();
      this.isEdit = false;
    }

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) { return; }

    const id = Number(idParam);
    if (isNaN(id)) { return; }

    this.clienteId = id;
    this.clienteService.getById(id).subscribe({
      next: (data) => {
        this.loadForm(data);
        this.isEdit = true;
        console.log('[ClienteForm] loaded cliente for edit', data);
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('[ClienteForm] failed to load cliente', err);
        this.resetForm();
        this.isEdit = false;
        this.cd.detectChanges();
      }
    });
  }

  ngOnChanges(): void {
    if (this.cliente) {
      this.loadForm(this.cliente);
      this.isEdit = !!this.cliente.id;
    } else {
      this.resetForm();
      this.isEdit = false;
    }
  }

  private loadForm(data: Cliente): void {
    this.formCliente = { ...data }; // Spread the full Cliente — includes real dates on edit
  }

  goBack(): void {
    this.location.back();
  }

  resetForm(): void {
    this.formCliente = {
      nombre: '',
      apellido: '',
      telefono: '',
      correo: '',
      direccion: ''
    };
  }

  onSubmit(): void {
    const payload: Partial<Cliente> = {
      nombre: this.formCliente.nombre ?? '',
      apellido: this.formCliente.apellido ?? '',
      telefono: this.formCliente.telefono ?? '',
      correo: this.formCliente.correo ?? '',
      direccion: this.formCliente.direccion ?? '',
    };

    if (this.isEdit && this.formCliente.id) {
      // Update Cliente and add id
      (payload).id = this.formCliente.id;

      this.clienteService.update(this.formCliente.id, payload).subscribe({
        next: () => {
          this.clienteSaved.emit();
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
      // Create new Cliente
      this.clienteService.create(payload).subscribe({
        next: () => {
          this.clienteSaved.emit();
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
