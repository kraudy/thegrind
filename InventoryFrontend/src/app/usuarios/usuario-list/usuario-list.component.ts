import { Component, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UsuarioService} from '../usuario.service';
import { UsuarioAdminDTO } from '../usuario-admin.model';
import { RouterLink } from '@angular/router';
import { Router } from '@angular/router';

import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-usuario-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './usuario-list.html',
  styleUrls: ['./usuario-list.css']
})
export class UsuarioListComponent implements OnInit {
  usuarios: UsuarioAdminDTO[] = [];
  loading = true;

  constructor(
    private usuarioService: UsuarioService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.loadUsuarios();
  }

  loadUsuarios() {
    this.usuarioService.getAll().subscribe({
      next: (data) => {
        this.usuarios = data || [];
        this.loading = false;
        console.log('[UsuarioList] loaded usuarios:', this.usuarios.length, this.usuarios);
        this.cdr.detectChanges(); // force view update if zone isn't triggering it
      },
      error: (err) => {
        // log full error to inspect status/code in the console
        console.error('[UsuarioList] failed to load usuarios', err);
        this.usuarios = [];
        this.loading = false;
        if (err?.status === 0) {
          console.log('Cannot reach backend (network error). Is the Spring server running?');
        } else if (err?.status) {
          console.log(`Backend error ${err.status}: ${err?.message || err?.statusText || 'unknown'}`);
        } else {
          console.log('Unexpected error loading usuarios (check console)');
        }
        this.cdr.detectChanges();
      }
    });
  }

  toggleActivo(u: UsuarioAdminDTO) {
    this.usuarioService.toggleActivo(u.usuario, !u.activo).subscribe({
      next: (updatedUser) => {
        // Update the user in the array with the returned data
        const index = this.usuarios.findIndex(user => user.usuario === u.usuario);
        if (index !== -1) {
          this.usuarios[index] = updatedUser;
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('[UsuarioList] failed to toggle activo', err);
        // Optionally reload all users on error to ensure consistency
        this.loadUsuarios();
      }
    });
  }

  editUsuario(u: UsuarioAdminDTO) {
    this.router.navigate(['/usuarios', u.usuario, 'edit']);
  }

  resetPassword(u: UsuarioAdminDTO) {
    const pass = prompt(`Nueva contraseña para ${u.usuario}:`);

    if (!pass || !pass.trim()) {
      return; // usuario canceló o dejó vacío
    }

    this.usuarioService.resetPassword(u.usuario, pass.trim()).subscribe({
      next: () => {
        this.toastService.showToast(
          'success',
          'Contraseña restablecida',
          `La contraseña de ${u.usuario} se ha actualizado correctamente`
        );
      },
      error: (err) => {
        console.error('[UsuarioList] failed to reset password', err);
        this.toastService.showToast(
          'error',
          'Error al restablecer contraseña',
          err.error?.message || 'No se pudo restablecer la contraseña'
        );
      }
    });
  }
}