import { Routes } from '@angular/router';
import { ProductListComponent } from './products/product-list/product-list.component';
import { ProductFormComponent } from './products/product-form/product-form.component';

import { OrdenListComponent } from './ordenes/orden-list/orden-list.component';
import { OrdenFormComponent } from './ordenes/orden-form/orden-form.component';

export const routes: Routes = [
  { path: '', redirectTo: '/products', pathMatch: 'full' },

  { path: 'products', component: ProductListComponent },

  { path: 'products/new', component: ProductFormComponent },
  { path: 'products/:id/edit', component: ProductFormComponent },

  { path: 'ordenes', component: OrdenListComponent },

  { path: 'ordenes/new', component: OrdenFormComponent },
  { path: 'ordenes/:id/edit', component: OrdenFormComponent }
];