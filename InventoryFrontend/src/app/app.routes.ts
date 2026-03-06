import { Routes } from '@angular/router';

import { HomeComponent } from './home/home/home.component';

import { ProductoListComponent } from './productos/producto-list/producto-list.component';
import { ProductoFormComponent } from './productos/producto-form/producto-form.component';

import { OrdenListComponent } from './ordenes/orden-list/orden-list.component';
import { OrdenFormComponent } from './ordenes/orden-form/orden-form.component';

import { OrdenDetalleListComponent } from './ordenes-detalle/orden-detalle-list/orden-detalle-list.component';
import { OrdenDetalleFormComponent } from './ordenes-detalle/orden-detalle-form/orden-detalle-form.component';

import { OrdenCalendarioListComponent } from './ordenes-calendario/orden-calendario-list/orden-calendario-list.component';
import { OrdenCalendarioFormComponent } from './ordenes-calendario/orden-calendario-form/orden-calendario-form.component';

import { OrdenSeguimientoListComponent } from './ordenes-seguimiento/orden-seguimiento-list/orden-seguimiento-list.component';
//import { OrdenSeguimientoFormComponent } from './ordenes-seguimiento/orden-seguimiento-form/orden-seguimiento-form.component';
import { OrdenSeguimientoImpresionListComponent } from './ordenes-seguimiento/orden-seguimiento-impresion-list/orden-seguimiento-impresion-list.component';

import { OrdenSeguimientoPreparacionListComponent } from './ordenes-seguimiento/orden-seguimiento-preparacion-list/orden-seguimiento-preparacion-list.component';
import { OrdenSeguimientoPreparacionDetalleListComponent } from './ordenes-seguimiento/orden-seguimiento-preparacion-detalle-list/orden-seguimiento-preparacion-detalle-list.component';

import { OrdenSeguimientoEntregaListComponent } from './ordenes-seguimiento/orden-seguimiento-entrega-list/orden-seguimiento-entrega-list.component';
import { OrdenSeguimientoEntregaDetalleListComponent } from './ordenes-seguimiento/orden-seguimiento-entrega-detalle-list/orden-seguimiento-entrega-detalle-list.component';

import { ClienteListComponent } from './clientes/cliente-list/cliente-list.component';
import { ClienteFormComponent } from './clientes/cliente-form/cliente-form.component';

import { ProductoTipoListComponent } from './productos-tipos/producto-tipo-list/producto-tipo-list.component';
import { ProductoTipoFormComponent } from './productos-tipos/producto-tipo-form/producto-tipo-form.component';

import { ProductoPrecioListComponent } from './productos-precios/producto-precio-list/producto-precio-list.component';
import { ProductoPrecioFormComponent } from './productos-precios/producto-precio-form/producto-precio-form.component';
import { OrdenSeguimientoImpresionDetalleListComponent } from './ordenes-seguimiento/orden-seguimiento-impresion-detalle-list/orden-seguimiento-impresion-detalle-list.component';

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
  { path: 'ordenes-detalle/:ordenId/:idOrdenDetalle/edit', component: OrdenDetalleFormComponent },

  { path: 'ordenes-calendario', component: OrdenCalendarioListComponent },
  { path: 'ordenes-calendario/:fecha/new', component: OrdenCalendarioFormComponent },
  //{ path: 'ordenes-calendario/:ordenId/new', component: OrdenCalendarioFormComponent },
  //{ path: 'ordenes-calendario/:ordenId/edit', component: OrdenCalendarioFormComponent },

  /* Seguimiento de ordenes */

  { path: 'ordenes-seguimiento/:idOrden/:clienteNombre', component: OrdenSeguimientoListComponent },

  { path: 'ordenes-seguimiento-impresion', component: OrdenSeguimientoImpresionListComponent },
  { path: 'ordenes-seguimiento-impresion/:idOrden/:clienteNombre', component: OrdenSeguimientoImpresionDetalleListComponent},

  { path: 'ordenes-seguimiento-preparacion', component: OrdenSeguimientoPreparacionListComponent },
  { path: 'ordenes-seguimiento-preparacion/:idOrden/:clienteNombre', component: OrdenSeguimientoPreparacionDetalleListComponent},

  { path: 'ordenes-seguimiento-entrega', component: OrdenSeguimientoEntregaListComponent },
  { path: 'ordenes-seguimiento-entrega/:idOrden/:clienteNombre', component: OrdenSeguimientoEntregaDetalleListComponent},

  { path: 'clientes', component: ClienteListComponent },
  { path: 'clientes/new', component: ClienteFormComponent },
  { path: 'clientes/:id/edit', component: ClienteFormComponent }
];