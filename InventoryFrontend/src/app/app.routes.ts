import { Routes } from '@angular/router';

import { LoginComponent } from './auth/auth/login.component';
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

import { OrdenSeguimientoRepartirListComponent } from './ordenes-seguimiento/orden-seguimiento-repartir-list/orden-seguimiento-repartir-list.component';
import { OrdenSeguimientoRepartirDetalleListComponent } from './ordenes-seguimiento/orden-seguimiento-repartir-detalle-list/orden-seguimiento-repartir-detalle-list.component';

import { ClienteListComponent } from './clientes/cliente-list/cliente-list.component';
import { ClienteFormComponent } from './clientes/cliente-form/cliente-form.component';

import { ProductoTipoListComponent } from './productos-tipos/producto-tipo-list/producto-tipo-list.component';
import { ProductoTipoFormComponent } from './productos-tipos/producto-tipo-form/producto-tipo-form.component';

import { ProductoPrecioFormComponent } from './productos-precios/producto-precio-form/producto-precio-form.component';
import { ProductoDetalleComponent } from './productos/producto-detalle/producto-detalle.component';

import { ProductoCostoFormComponent } from './productos-costos/producto-costo-form/producto-costo-form.component';

import { OrdenSeguimientoImpresionDetalleListComponent } from './ordenes-seguimiento/orden-seguimiento-impresion-detalle-list/orden-seguimiento-impresion-detalle-list.component';
import { OrdenSeguimientoGeneralListComponent } from './ordenes-seguimiento/orden-seguimiento-general-list/orden-seguimiento-general-list.component';
import { OrdenPagoAprobarListComponent } from './ordenes-pago/orden-pago-list/orden-pago-aprobar-list.component';

import { OrdenFacturacionDetalleListComponent } from './ordenes-facturacion/orden-facturacion-detalle-list/orden-facturacion-detalle-list.component';
import { OrdenFacturacionListComponent } from './ordenes-facturacion/orden-facturacion-list/orden-facturacion-list.component';

import { OrdenCostoListComponent } from './ordenes-costo/orden-costo-list/orden-costo-list.component';
import { OrdenCostoFormComponent } from './ordenes-costo/orden-costo-form/orden-costo-form.component';

import { FacturaListComponent } from './facturas/factura-list/factura-list.component';

import { UsuarioListComponent } from './usuarios/usuario-list/usuario-list.component';
import { UsuarioFormComponent } from './usuarios/usuario-form/usuario-form.component';

import { authGuard } from './auth/auth.guard';



export const routes: Routes = [
  { path: '', component: LoginComponent },

  { path: 'home', component: HomeComponent, canActivate: [authGuard] }, 

  { path: 'productos', component: ProductoListComponent, canActivate: [authGuard] },
  { path: 'productos/new', component: ProductoFormComponent, canActivate: [authGuard] },
  { path: 'productos/:id/edit', component: ProductoFormComponent, canActivate: [authGuard] },

  { path: 'productos-detalle/:id', component: ProductoDetalleComponent, canActivate: [authGuard] },
  
  { path: 'productos-tipos', component: ProductoTipoListComponent, canActivate: [authGuard] },
  { path: 'productos-tipos/new', component: ProductoTipoFormComponent, canActivate: [authGuard] },
  { path: 'productos-tipos/:tipo/edit', component: ProductoTipoFormComponent, canActivate: [authGuard] },

  { path: 'productos-precios/new', component: ProductoPrecioFormComponent, canActivate: [authGuard] },
  { path: 'productos-precios/:productoId/:precio/edit', component: ProductoPrecioFormComponent, canActivate: [authGuard] },

  { path: 'productos-costos/new', component: ProductoCostoFormComponent, canActivate: [authGuard] },
  { path: 'productos-costos/:productoId/:tipoCosto/edit', component: ProductoCostoFormComponent, canActivate: [authGuard] },

  { path: 'ordenes', component: OrdenListComponent, canActivate: [authGuard] },
  { path: 'ordenes/new', component: OrdenFormComponent, canActivate: [authGuard] },
  { path: 'ordenes/:id/edit', component: OrdenFormComponent, canActivate: [authGuard] },

  { path: 'ordenes-detalle/:ordenId', component: OrdenDetalleListComponent, canActivate: [authGuard] },
  { path: 'ordenes-detalle/:ordenId/new', component: OrdenDetalleFormComponent, canActivate: [authGuard] },
  { path: 'ordenes-detalle/:ordenId/:idOrdenDetalle/edit', component: OrdenDetalleFormComponent, canActivate: [authGuard] },

  { path: 'ordenes-calendario', component: OrdenCalendarioListComponent, canActivate: [authGuard] },
  { path: 'ordenes-calendario/:fecha/new', component: OrdenCalendarioFormComponent, canActivate: [authGuard] },
  //{ path: 'ordenes-calendario/:ordenId/new', component: OrdenCalendarioFormComponent, canActivate: [authGuard] },
  //{ path: 'ordenes-calendario/:ordenId/edit', component: OrdenCalendarioFormComponent, canActivate: [authGuard] },

  /* Pago de ordenes */

  { path: 'ordenes-pago/aprobar', component: OrdenPagoAprobarListComponent, canActivate: [authGuard] },

  /* Costo de ordenes */
  { path: 'ordenes-costo/pagar/:tipoCosto/:trabajador', component: OrdenCostoListComponent, canActivate: [authGuard] },
  { path: 'ordenes-costo/pagar', component: OrdenCostoListComponent, canActivate: [authGuard] },
  { path: 'ordenes-costo/pagar/confirmar', component: OrdenCostoFormComponent, canActivate: [authGuard] },

  /* Facturacion de ordenes */

  { path: 'ordenes-facturacion', component: OrdenFacturacionListComponent, canActivate: [authGuard] },
  { path: 'ordenes-facturacion/:idOrden/:clienteNombre', component: OrdenFacturacionDetalleListComponent, canActivate: [authGuard] },

  /* Seguimiento de ordenes */

  { path: 'ordenes-seguimiento/:idOrden/:clienteNombre', component: OrdenSeguimientoListComponent, canActivate: [authGuard] },

  { path: 'ordenes-seguimiento-impresion', component: OrdenSeguimientoImpresionListComponent, canActivate: [authGuard] },
  { path: 'ordenes-seguimiento-impresion/:idOrden/:clienteNombre', component: OrdenSeguimientoImpresionDetalleListComponent, canActivate: [authGuard] },

  { path: 'ordenes-seguimiento-preparacion', component: OrdenSeguimientoPreparacionListComponent, canActivate: [authGuard] },
  { path: 'ordenes-seguimiento-preparacion/:idOrden/:clienteNombre', component: OrdenSeguimientoPreparacionDetalleListComponent, canActivate: [authGuard] },

  { path: 'ordenes-seguimiento-entrega', component: OrdenSeguimientoEntregaListComponent, canActivate: [authGuard] },
  { path: 'ordenes-seguimiento-entrega/:idOrden/:clienteNombre', component: OrdenSeguimientoEntregaDetalleListComponent, canActivate: [authGuard] },

  { path: 'ordenes-seguimiento-repartir', component: OrdenSeguimientoRepartirListComponent, canActivate: [authGuard] },
  { path: 'ordenes-seguimiento-repartir/:idOrden/:clienteNombre', component: OrdenSeguimientoRepartirDetalleListComponent, canActivate: [authGuard] },

  { path: 'clientes', component: ClienteListComponent, canActivate: [authGuard] },
  { path: 'clientes/new', component: ClienteFormComponent, canActivate: [authGuard] },
  { path: 'clientes/:id/edit', component: ClienteFormComponent, canActivate: [authGuard] },

  { path: 'ordenes-seguimiento/general', component: OrdenSeguimientoGeneralListComponent, canActivate: [authGuard] },

  // Facturas
  { path: 'facturas', component: FacturaListComponent, canActivate: [authGuard] },

  // Usuarios
  { path: 'usuarios', component: UsuarioListComponent, canActivate: [authGuard] },
  { path: 'usuarios/new', component: UsuarioFormComponent, canActivate: [authGuard] },
  { path: 'usuarios/:usuario/edit', component: UsuarioFormComponent, canActivate: [authGuard] },

  // Catch-all → must be LAST
  { path: '**', redirectTo: '' }
];