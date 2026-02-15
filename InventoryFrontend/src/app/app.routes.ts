import { Routes } from '@angular/router';

import { HomeComponent } from './home/home/home.component';

import { ProductoListComponent } from './productos/producto-list/producto-list.component';
import { ProductoFormComponent } from './productos/producto-form/producto-form.component';

import { OrdenListComponent } from './ordenes/orden-list/orden-list.component';
import { OrdenFormComponent } from './ordenes/orden-form/orden-form.component';

import { OrdenDetalleListComponent } from './ordenes-detalle/orden-detalle-list/orden-detalle-list.component';
import { OrdenDetalleFormComponent } from './ordenes-detalle/orden-detalle-form/orden-detalle-form.component';

import { ClienteListComponent } from './clientes/cliente-list/cliente-list.component';
import { ClienteFormComponent } from './clientes/cliente-form/cliente-form.component';

import { ProductoTipoListComponent } from './productos-tipos/producto-tipo-list/producto-tipo-list.component';
import { ProductoTipoFormComponent } from './productos-tipos/producto-tipo-form/producto-tipo-form.component';

import { ProductoPrecioListComponent } from './productos-precios/producto-precio-list/producto-precio-list.component';
import { ProductoPrecioFormComponent } from './productos-precios/producto-precio-form/producto-precio-form.component';

export const routes: Routes = [
  { path: '', component: HomeComponent }, 

  { path: 'productos', component: ProductoListComponent },
  { path: 'productos/new', component: ProductoFormComponent },
  { path: 'productos/:id/edit', component: ProductoFormComponent },

  { path: 'productos-tipos', component: ProductoTipoListComponent },
  { path: 'productos-tipos/new', component: ProductoTipoFormComponent },
  { path: 'productos-tipos/:tipo/edit', component: ProductoTipoFormComponent },

  { path: 'productos-precios', component: ProductoPrecioListComponent },
  { path: 'productos-precios/new', component: ProductoPrecioFormComponent },
  { path: 'productos-precios/:productoId/:precio/edit', component: ProductoPrecioFormComponent },

  { path: 'ordenes', component: OrdenListComponent },
  { path: 'ordenes/new', component: OrdenFormComponent },
  { path: 'ordenes/:id/edit', component: OrdenFormComponent },

  { path: 'ordenes-detalle/:ordenId', component: OrdenDetalleListComponent },
  { path: 'ordenes-detalle/:ordenId/new', component: OrdenDetalleFormComponent },
  { path: 'ordenes-detalle/:ordenId/:idOrdenDetalle/:idProducto/edit', component: OrdenDetalleFormComponent },

  { path: 'clientes', component: ClienteListComponent },
  { path: 'clientes/new', component: ClienteFormComponent },
  { path: 'clientes/:id/edit', component: ClienteFormComponent }
];