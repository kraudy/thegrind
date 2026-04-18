import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UsuarioService } from '../usuario.service';
import { UsuarioAdminDTO } from '../usuario-admin.model';
import { CreateUsuarioRequest } from '../usuario-create.model';
import { ChangeDetectorRef } from '@angular/core';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-usuario-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './usuario-form.html',
  styleUrls: ['./usuario-form.css']
})
export class UsuarioFormComponent implements OnInit {
  usuario = '';
  password = '';
  activo = true;
  roles: string[] = [];
  allRoles: string[] = [];
  isEdit = false;
  loading = false;
  error = '';

  private originalRoles: string[] = [];
  private originalActivo = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private usuarioService: UsuarioService,
    private cdr: ChangeDetectorRef,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.loadAllRoles();

    const usuarioParam = this.route.snapshot.paramMap.get('usuario');
    if (usuarioParam) {
      this.isEdit = true;
      this.usuario = usuarioParam;
      this.loadUser(usuarioParam);
    }
  }

  private loadAllRoles() {
    this.usuarioService.getAllRoles().subscribe({
      next: (roles) => {
        this.allRoles = roles; 
        this.cdr.detectChanges();
      },
      error: () => {
        console.error('No se pudieron cargar los roles');
        this.cdr.detectChanges();
      }
    });
  }

  private loadUser(usuario: string) {
    this.usuarioService.getById(usuario).subscribe({
      next: (data) => {
        this.activo = data.activo;
        this.originalActivo = data.activo;
        this.roles = [...data.roles];
        this.originalRoles = [...data.roles];
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Usuario no encontrado';
        this.cdr.detectChanges();
      }
    });
  }

  toggleRole(rol: string) {
    if (this.roles.includes(rol)) {
      this.roles = this.roles.filter(r => r !== rol);
    } else {
      this.roles.push(rol);
    }
    this.cdr.detectChanges();
  }

  onSubmit() {
    if (this.loading) return;
    this.loading = true;
    this.error = '';

    if (this.isEdit) {
      this.updateUser();
    } else {
      this.createUser();
    }
  }

  private updateUser() {
    // 1. Actualizar estado activo
    this.usuarioService.toggleActivo(this.usuario, this.activo).subscribe({
      next: () => this.updateRoles(),
      error: (err) => {
        console.error(err);
        this.updateRoles(); // seguimos con los roles aunque falle el activo
      }
    });
  }

  private updateRoles() {
    const toAdd = this.roles.filter(r => !this.originalRoles.includes(r));
    const toRemove = this.originalRoles.filter(r => !this.roles.includes(r));

    const total = toAdd.length + toRemove.length;
    if (total === 0) {
      this.finishSave();
      return;
    }

    let completed = 0;
    const checkComplete = () => {
      completed++;
      if (completed === total) this.finishSave();
    };

    toAdd.forEach(rol => {
      this.usuarioService.assignRole(this.usuario, rol).subscribe({
        next: checkComplete,
        error: (e) => { console.error(e); checkComplete(); }
      });
    });

    toRemove.forEach(rol => {
      this.usuarioService.removeRole(this.usuario, rol).subscribe({
        next: checkComplete,
        error: (e) => { console.error(e); checkComplete(); }
      });
    });
  }

  private createUser() {
    const req: CreateUsuarioRequest = {
      usuario: this.usuario,
      password: this.password,
      activo: this.activo,
      roles: this.roles
    };

    this.usuarioService.create(req).subscribe({
      next: () => {
        this.toastService.showToast(
          'success',
          'Usuario creado',
          `El usuario ${this.usuario} se ha creado correctamente`
        );
        this.router.navigate(['/usuarios']);
      },
      error: (err) => {
        this.loading = false;
        const errorMsg = err.error?.message || 'Error al crear el usuario';
        this.toastService.showToast(
          'error',
          'Error al crear usuario',
          errorMsg
        );
        // Opcional: también mostrar el mensaje rojo debajo del formulario
        this.error = errorMsg;
      }
    });
  }

  private finishSave() {
    this.loading = false;
    this.toastService.showToast(
      'success',
      this.isEdit ? 'Usuario actualizado' : 'Usuario creado',
      this.isEdit 
        ? `Los cambios en ${this.usuario} se guardaron correctamente` 
        : `El usuario ${this.usuario} se ha creado correctamente`
    );
    this.router.navigate(['/usuarios']);
  }

  public cancel() {
    this.router.navigate(['/usuarios']);
  }
}