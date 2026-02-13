import { Routes } from '@angular/router';

import { ProductoListComponent } from './productos/producto-list/producto-list.component';
import { ProductoFormComponent } from './productos/producto-form/producto-form.component';

import { OrdenListComponent } from './ordenes/orden-list/orden-list.component';
import { OrdenFormComponent } from './ordenes/orden-form/orden-form.component';

import { ClienteListComponent } from './clientes/cliente-list/cliente-list.component';
import { ClienteFormComponent } from './clientes/cliente-form/cliente-form.component';

import { ProductoTipoListComponent } from './productos-tipos/producto-tipo-list/producto-tipo-list.component';
import { ProductoTipoFormComponent } from './productos-tipos/producto-tipo-form/producto-tipo-form.component';

import { ProductoPrecioListComponent } from './productos-precios/producto-precio-list/producto-precio-list.component';
import { ProductoPrecioFormComponent } from './productos-precios/producto-precio-form/producto-precio-form.component';

export const routes: Routes = [
  { path: '', redirectTo: '/ordenes', pathMatch: 'full' }, // Change this for landing page

  { path: 'productos', component: ProductoListComponent },
  { path: 'productos/new', component: ProductoFormComponent },
  { path: 'productos/:id/edit', component: ProductoFormComponent },

  { path: 'productos-tipos', component: ProductoTipoListComponent },
  { path: 'productos-tipos/new', component: ProductoTipoFormComponent },
  { path: 'productos-tipos/:tipo/edit', component: ProductoTipoFormComponent },

  { path: 'productos-precios', component: ProductoPrecioListComponent },
  { path: 'productos-precios/new', component: ProductoPrecioFormComponent },
  { path: 'productos-precios/:id/edit', component: ProductoPrecioFormComponent },

  { path: 'ordenes', component: OrdenListComponent },
  { path: 'ordenes/new', component: OrdenFormComponent },
  { path: 'ordenes/:id/edit', component: OrdenFormComponent },

  { path: 'clientes', component: ClienteListComponent },
  { path: 'clientes/new', component: ClienteFormComponent },
  { path: 'clientes/:id/edit', component: ClienteFormComponent }
];