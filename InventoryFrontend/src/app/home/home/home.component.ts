import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent implements OnInit {

  userRoles: string[] = [];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.userRoles = this.authService.getRoles();
  }

  // Helper: admin OR specific role
  hasAccess(requiredRole: string): boolean {
    return this.userRoles.includes('admin') || this.userRoles.includes(requiredRole);
  }

  logout() {
    this.authService.logout();           // clear token & roles
    this.router.navigate(['/login']);    // redirect to login
  }
}