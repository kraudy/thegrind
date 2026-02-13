import { Component, Input, Output, EventEmitter, OnChanges, OnInit, ChangeDetectorRef} from '@angular/core';

import { CommonModule, Location  } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-producto-precio-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './producto-precio-form.html',
  styleUrls: ['./producto-precio-form.css'],
})
export class ProductoPrecioFormComponent {

}
