package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.model.OrdenTrabajo;
import com.github.kraudy.InventoryBackend.model.OrdenTrabajoPK;
import com.github.kraudy.InventoryBackend.dto.UsuarioDTO;
import com.github.kraudy.InventoryBackend.model.OrdenDetalle;
import com.github.kraudy.InventoryBackend.model.OrdenDetallePK;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimiento;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoHistorico;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoPK;
import com.github.kraudy.InventoryBackend.model.Producto;
import com.github.kraudy.InventoryBackend.model.ProductoPrecio;
import com.github.kraudy.InventoryBackend.model.ProductoTipoEstado;
import com.github.kraudy.InventoryBackend.model.ProductoTipoEstadoPK;
import com.github.kraudy.InventoryBackend.repository.OrdenTrabajoRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenDetalleRepository;
import com.github.kraudy.InventoryBackend.repository.ProductoTipoEstadoRepository;
import com.github.kraudy.InventoryBackend.repository.UsuarioRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenSeguimientoRepository;
import com.github.kraudy.InventoryBackend.repository.ProductoPrecioRepository;
import com.github.kraudy.InventoryBackend.repository.ProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ordenes-trabajo")
@CrossOrigin(origins = "http://localhost:4200")  // Prevents Cors error. Allow Angular dev server
public class OrdenTrabajoController {
  
  @Autowired
  private OrdenTrabajoRepository ordenTrabajoRepository;
  
  @Autowired
  private OrdenSeguimientoRepository ordenSeguimientoRepository;
  
  @Autowired
  private ProductoTipoEstadoRepository productoTipoEstadoRepository;

  @Autowired
  private OrdenDetalleRepository ordenDetalleRepository;

  @Autowired
  private UsuarioRepository usuarioRepository;


  /* Registra progreso de trabajo, aplica para reparadores, normal y pegado */
  @PostMapping("/registrar-trabajo/{idOrden}/{idOrdenDetalle}/{estado}/{cantidadTrabajada}")
  public OrdenTrabajo registrarProgreso(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @PathVariable String estado,
          @PathVariable int cantidadTrabajada) {

      OrdenTrabajoPK pk = new OrdenTrabajoPK(idOrden, idOrdenDetalle, estado);
      OrdenTrabajo trabajo = ordenTrabajoRepository.findById(pk)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay trabajo asignado para este estado"));

      if (cantidadTrabajada < 0 || cantidadTrabajada > trabajo.getCantidadAsignada()) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantidad inválida");
      }

      trabajo.setCantidadTrabajada(cantidadTrabajada + trabajo.getCantidadTrabajada());
      if (trabajo.getCantidadTrabajada() > trabajo.getCantidadAsignada()) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad trabajada no puede exceder la cantidad asignada");
      }

      trabajo.setCantidadNoTrabajada(trabajo.getCantidadAsignada() - trabajo.getCantidadTrabajada());

      //trabajo.setComentario(dto.comentario());

      return ordenTrabajoRepository.save(trabajo);
  }

  @GetMapping("/obtener-reparador/{idOrden}/{idOrdenDetalle}")
  public UsuarioDTO getReparadores(
            @PathVariable Long idOrden, 
            @PathVariable Long idOrdenDetalle) {

    return ordenTrabajoRepository.getReparador(idOrden, idOrdenDetalle);
  }

  @GetMapping("/obtener-normal/{idOrden}/{idOrdenDetalle}")
  public UsuarioDTO getNormal(
            @PathVariable Long idOrden, 
            @PathVariable Long idOrdenDetalle) {

    return ordenTrabajoRepository.getNormal(idOrden, idOrdenDetalle);
  }

  @PostMapping("/asignar-normal/{idOrden}/{idOrdenDetalle}/{normal}")
  public OrdenTrabajo asignarNormal(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @PathVariable String normal) {

    OrdenTrabajo trabajoReparacion = new OrdenTrabajo();
    
    return trabajoReparacion;
  }
  
  // Avanzar detalle
  //  al siguiente estado
  @PostMapping("/asignar-reparacion/{idOrden}/{idOrdenDetalle}/{reparador}")
  public OrdenTrabajo asignarReparacion(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @PathVariable String reparador) {
            
    // Obtencion de detalle de orden
    OrdenDetallePK pk = new OrdenDetallePK(idOrden, idOrdenDetalle);

    OrdenDetalle detalleReparacion = ordenDetalleRepository.findById(pk)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Detalle no encontrado"));

    // Obtencion de seguimiento del detalle
    OrdenSeguimientoPK ordenSeguimientoPK = new OrdenSeguimientoPK(idOrden, idOrdenDetalle);

    OrdenSeguimiento ordenSeguimientoActual = ordenSeguimientoRepository.findById(ordenSeguimientoPK).
      orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El detalle no se encuentra en seguimiento"));
   
    // Obtenemos el siguiente estado
    ProductoTipoEstadoPK productoTipoEstadoPK = new ProductoTipoEstadoPK(ordenSeguimientoActual.getTipo(), ordenSeguimientoActual.getSubTipo(), ordenSeguimientoActual.getSecuencia() + 1);
    
    ProductoTipoEstado productoTipoEstadoSiguiente = productoTipoEstadoRepository.findById(productoTipoEstadoPK).
      orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Siguiente estado no encontrado"));

    if (!productoTipoEstadoSiguiente.getEstado().equals("Reparacion")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El siguiente estado no es de reparacion");
    }

    if (!usuarioRepository.usuarioEsReparador(reparador)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no es un reparador");
    }
  
      //TODO: Maybe i could call avanzar direclty from here, we'll see
    OrdenTrabajo trabajoReparacion = new OrdenTrabajo();

    trabajoReparacion.setIdOrden(idOrden);
    trabajoReparacion.setIdOrdenDetalle(idOrdenDetalle);
    trabajoReparacion.setEstado(productoTipoEstadoSiguiente.getEstado());
    trabajoReparacion.setSecuencia(productoTipoEstadoSiguiente.getSecuencia());
    trabajoReparacion.setTrabajador(reparador);
    trabajoReparacion.setRol("repara");
    trabajoReparacion.setIdProducto(detalleReparacion.getIdProducto());
    // Por ahora le ponomes la cantidad del detalle.
    trabajoReparacion.setCantidadAsignada(detalleReparacion.getCantidad());

    // Inicialmente, toda la cantidad esta sin trabajar, a medida que el reparador vaya avanzando en el trabajo, se iran actualizando estos campos
    trabajoReparacion.setCantidadTrabajada(0);
    trabajoReparacion.setCantidadNoTrabajada(detalleReparacion.getCantidad());

    trabajoReparacion.setComentario("");
    trabajoReparacion.setFechaTrabajo(LocalDate.now());

    // pendiente por ahora
    //trabajoReparacion.setIdSeguimiento();

    return ordenTrabajoRepository.save(trabajoReparacion);

  }
}