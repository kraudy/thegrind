import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  usuario = '';
  password = '';
  error = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onLogin() {
    this.error = ''; // clear previous error

    this.authService.login(this.usuario, this.password).subscribe({
      next: () => {
        this.router.navigate(['/']);   // go to home
      },
      error: (err: HttpErrorResponse) => {
        console.error('Login error:', err); // optional for debugging

        // Any 401, 403 or any auth-related error → friendly message
        if (err.status === 401 || err.status === 403 || err.status === 400) {
          this.error = 'Usuario o Contraseña incorrecto';
        } else {
          this.error = 'Error al iniciar sesión. Inténtalo de nuevo.';
        }
      }
    });
  }
}