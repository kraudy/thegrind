import { Component, Input, Output, EventEmitter, OnChanges, OnInit, ChangeDetectorRef } from '@angular/core';

import { CommonModule, Location  } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { OrdenService } from '../orden.service';
import { Orden } from '../orden.model';

import { ClienteService } from '../../clientes/cliente.service';
import { Cliente } from '../../clientes/cliente.model';

import { NotificationService } from '../../shared/notification.service'; 
import { ToastService } from '../../shared/toast/toast.service';

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

  // Canal context: derived from query param when creating, or from loaded orden when editing
  canal: 'General' | 'Whatsapp' = 'General';

  get isWhatsappContext(): boolean {
    return this.canal === 'Whatsapp';
  }

  get canalQueryParams(): any {
    return this.canal === 'Whatsapp' ? { canal: 'Whatsapp' } : {};
  }

  //Autocomplete properties
  filteredClientes: Cliente[] = [];
  selectedCliente: Cliente | null = null;
  searchTerm: string = '';
  private searchTerms = new Subject<string>();

  // Fecha/hora de vencimiento (split UI: date input + time dropdown)
  fechaVencimientoDate: string = ''; // YYYY-MM-DD
  fechaVencimientoTime: string = ''; // HH:MM (24h, internal value)
  timeSlots: { value: string; label: string }[] = [];
  
  constructor(
    private ordenService: OrdenService,
    private clienteService: ClienteService,
    private location: Location,
    private route: ActivatedRoute,
    private router: Router,
    private cd: ChangeDetectorRef,
    private toastService: ToastService
  ) {}

  formOrden: Partial<Orden> = { 
    idCliente: 0,
    fechaVencimiento: null
  };

  // Get minimum date for fechaVencimiento (current date/time)
  get minFechaVencimiento(): string {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`; // Format: YYYY-MM-DDTHH:MM
  }

  // Minimum selectable date (today, YYYY-MM-DD)
  get minFechaVencimientoDate(): string {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  // Build valid working-hour time slots (8:00 AM - 5:30 PM, every 5 minutes)
  // Lunch break excluded: 12:05 PM - 1:25 PM (no slots in that range)
  private buildTimeSlots(): { value: string; label: string }[] {
    const slots: { value: string; label: string }[] = [];
    for (let h = 8; h <= 17; h++) {
      for (let m = 0; m < 60; m += 5) {
        if (h === 17 && m > 30) break;
        const totalMin = h * 60 + m;
        // Skip lunch break: 12:05 PM (725) through 1:25 PM (805) inclusive
        if (totalMin >= 12 * 60 + 5 && totalMin <= 13 * 60 + 25) continue;
        const hh = String(h).padStart(2, '0');
        const mm = String(m).padStart(2, '0');
        const value = `${hh}:${mm}`;
        const period = h >= 12 ? 'PM' : 'AM';
        const h12 = h % 12 === 0 ? 12 : h % 12;
        const label = `${h12}:${mm} ${period}`;
        slots.push({ value, label });
      }
    }
    return slots;
  }

  // Combine date + time into formOrden.fechaVencimiento ("YYYY-MM-DDTHH:MM")
  private updateFechaVencimiento(): void {
    if (this.fechaVencimientoDate && this.fechaVencimientoTime) {
      this.formOrden.fechaVencimiento = `${this.fechaVencimientoDate}T${this.fechaVencimientoTime}` as unknown as Date;
    } else {
      this.formOrden.fechaVencimiento = null;
    }
  }

  onFechaVencimientoDateChange(value: string): void {
    this.fechaVencimientoDate = value;
    this.updateFechaVencimiento();
  }

  onFechaVencimientoTimeChange(value: string): void {
    this.fechaVencimientoTime = value;
    this.updateFechaVencimiento();
  }

  // Snap a "HH:MM" string to the nearest valid 5-min slot within working hours
  private snapToValidSlot(hh: number, mm: number): string {
    // Clamp to working window
    let totalMin = hh * 60 + mm;
    const minWindow = 8 * 60;       // 08:00
    const maxWindow = 17 * 60 + 30; // 17:30
    const lunchStart = 12 * 60 + 5; // 12:05
    const lunchEnd = 13 * 60 + 25;  // 13:25
    if (totalMin < minWindow) totalMin = minWindow;
    if (totalMin > maxWindow) totalMin = maxWindow;
    // Snap to nearest 5-min boundary
    totalMin = Math.round(totalMin / 5) * 5;
    if (totalMin > maxWindow) totalMin = maxWindow;
    // If snapped into lunch break, move to nearest edge (12:00 or 13:30)
    if (totalMin >= lunchStart && totalMin <= lunchEnd) {
      const distToBefore = totalMin - (12 * 60);   // distance to 12:00
      const distToAfter = (13 * 60 + 30) - totalMin; // distance to 13:30
      totalMin = distToBefore <= distToAfter ? 12 * 60 : 13 * 60 + 30;
    }
    const nh = Math.floor(totalMin / 60);
    const nm = totalMin % 60;
    return `${String(nh).padStart(2, '0')}:${String(nm).padStart(2, '0')}`;
  }

  private hydrateFechaVencimientoParts(): void {
    const raw = this.formOrden.fechaVencimiento;
    if (!raw) {
      this.fechaVencimientoDate = '';
      this.fechaVencimientoTime = '';
      return;
    }
    const d = raw instanceof Date ? raw : new Date(raw as unknown as string);
    if (isNaN(d.getTime())) {
      this.fechaVencimientoDate = '';
      this.fechaVencimientoTime = '';
      return;
    }
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    this.fechaVencimientoDate = `${year}-${month}-${day}`;
    this.fechaVencimientoTime = this.snapToValidSlot(d.getHours(), d.getMinutes());
    this.updateFechaVencimiento();
  }

  ngOnInit(): void {
    this.timeSlots = this.buildTimeSlots();

    // Canal viene en el queryParam (?canal=Whatsapp) cuando se crea desde la tarjeta Whatsapp
    const canalParam = this.route.snapshot.queryParamMap.get('canal');
    if (canalParam === 'Whatsapp') {
      this.canal = 'Whatsapp';
    }

    if (!this.orden) {
      this.resetForm();
      this.isEdit = false;
    }

    //Debounced search setup — same filter logic as cliente-list:
    // numeric term → filter by id, otherwise → filter by nombre.
    this.searchTerms.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        const t = (term || '').trim();
        if (!t) return of([]);
        let filterId: number | undefined;
        let filterNombre: string | undefined;
        const num = Number(t);
        if (!isNaN(num) && num > 0 && Number.isInteger(num)) {
          filterId = num;
        } else {
          filterNombre = t;
        }
        return this.clienteService.getAllWithFilters({
          id: filterId,
          nombre: filterNombre,
        });
      })
    ).subscribe(clientes => {
      this.filteredClientes = clientes || [];
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
        // En edición, el canal viene de la orden ya creada
        if (data.canal === 'Whatsapp') {
          this.canal = 'Whatsapp';
        } else {
          this.canal = 'General';
        }

        this.loadClienteForDisplay();
        this.hydrateFechaVencimientoParts();
        
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
      this.hydrateFechaVencimientoParts();
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
      fechaVencimiento: null
    };

    // Reset autocomplete state
    this.searchTerm = '';
    this.selectedCliente = null;
    this.filteredClientes = [];

    // Reset fecha/hora pickers
    this.fechaVencimientoDate = '';
    this.fechaVencimientoTime = '';
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
      fechaVencimiento: this.formOrden.fechaVencimiento,
      canal: this.canal
    };

    if (this.isEdit && this.formOrden.id) {
      // Update Orden and add id
      (payload).id = this.formOrden.id;

      this.ordenService.update(this.formOrden.id, payload).subscribe({
        next: () => {
          this.toastService.showToast('success', 'Orden actualizada', `La orden #${this.formOrden.id} para ${this.selectedCliente?.nombre || 'cliente'} ha sido actualizada exitosamente`, 4000);
          this.ordenSaved.emit();
          this.goBack();
        },
        complete: () => {
          console.log('Orden payload send sucessfully for update:', payload);
        },
        error: (err) => {
          console.error('Failed to update orden', err);
          this.toastService.showToast('error', 'Error al actualizar orden', err?.error?.message || 'No se pudo actualizar la orden', 7000);
        }
      });
    } else {
      // Create new Orden
      this.ordenService.create(payload).subscribe({
        next: (createdOrden) => {
          this.toastService.showToast('success', 'Orden creada', `La orden #${createdOrden.id} para ${this.selectedCliente?.nombre || 'cliente'} ha sido creada exitosamente`, 4000);
          this.ordenSaved.emit();
          this.resetForm();
          // Navigate to orden-detalle to add details
          this.router.navigate(['/ordenes-detalle', createdOrden.id], {
            queryParams: this.canalQueryParams
          });
        },
        complete: () => {
          console.log('Orden payload send sucessfully for create:', payload);
        },
        error: (err) => {
          console.error('Failed to create orden', err);
          this.toastService.showToast('error', 'Error al crear orden', err?.error?.message || 'No se pudo crear la orden', 7000);
        }
      });
    }
  }

}
