import { Component } from '@angular/core';
import { Products } from './products/products';  // Adjust path if your file is named differently

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [Products],  // This lets us use <app-products> in the template
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
}