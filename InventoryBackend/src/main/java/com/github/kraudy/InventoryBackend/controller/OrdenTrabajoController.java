package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.model.OrdenTrabajo;
import com.github.kraudy.InventoryBackend.model.OrdenTrabajoPK;
import com.github.kraudy.InventoryBackend.dto.UsuarioDTO;
import com.github.kraudy.InventoryBackend.model.OrdenDetalle;
import com.github.kraudy.InventoryBackend.model.OrdenDetallePK;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimiento;
import com.github.kraudy.InventoryBackend.model.OrdenCalendario;
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
import com.github.kraudy.InventoryBackend.repository.OrdenCalendarioRepository;
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
  private OrdenCalendarioRepository ordenCalendarioRepository;
  
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
  @PostMapping("/progreso-trabajo/{idOrden}/{idOrdenDetalle}/{cantidadTrabajada}")
  public OrdenTrabajo progresoTrabajo(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @PathVariable int cantidadTrabajada) {

    // Obtencion de seguimiento del detalle para saber el estado actual y hacer las validaciones correspondientes
    OrdenSeguimiento ordenSeguimientoActual = ordenSeguimientoRepository.findById(new OrdenSeguimientoPK(idOrden, idOrdenDetalle)).
          orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El detalle no se encuentra en seguimiento"));
   
    ProductoTipoEstadoPK productoTipoEstadoSiguientePK = new ProductoTipoEstadoPK(ordenSeguimientoActual.getTipo(), ordenSeguimientoActual.getSubTipo(), ordenSeguimientoActual.getSecuencia() + 1);
    ProductoTipoEstado productoTipoEstadoSiguiente = productoTipoEstadoRepository.findById(productoTipoEstadoSiguientePK).
      orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Siguiente estado no encontrado"));

    ProductoTipoEstadoPK productoTipoEstadoAnteriorPK = new ProductoTipoEstadoPK(ordenSeguimientoActual.getTipo(), ordenSeguimientoActual.getSubTipo(), ordenSeguimientoActual.getSecuencia() - 1);
    ProductoTipoEstado productoTipoEstadoAnterior = productoTipoEstadoRepository.findById(productoTipoEstadoAnteriorPK).
      orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado anterior no encontrado"));

    // Se valida estado anterior y siguiente. Esto es conveniente para filtrar otros productos que puedan tener algun estado en comun pero que son de otro flujo.
    // Se agrega listo para incluir a las ampliaciones
    if (!List.of("Pegado", "Enmarcado", "Listo").contains(productoTipoEstadoSiguiente.getEstado())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El siguiente estado del producto no es valido para progreso de trabajo: " + productoTipoEstadoSiguiente.getEstado());
    }

    //TODO: validar si es necesario meter Impresion aqui
    if (!List.of("Reparacion", "Normal").contains(productoTipoEstadoAnterior.getEstado())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado anterior del producto no es valido para progreso de trabajo: " + productoTipoEstadoAnterior.getEstado());
    }

    // estado anterior por ahora deberia ser "Reparacion" o "Normal"
    OrdenTrabajoPK pk = new OrdenTrabajoPK(idOrden, idOrdenDetalle, productoTipoEstadoAnterior.getEstado());

    OrdenTrabajo trabajo = ordenTrabajoRepository.findById(pk)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay trabajo asignado para este estado"));

    if (cantidadTrabajada <= 0 || cantidadTrabajada + trabajo.getCantidadTrabajada() > trabajo.getCantidadAsignada()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantidad trabajada inválida: "  + cantidadTrabajada);
    }

    // Agrega progreso al trabajo actual
    trabajo.setCantidadTrabajada(trabajo.getCantidadTrabajada() + cantidadTrabajada);

    trabajo.setCantidadNoTrabajada(trabajo.getCantidadAsignada() - trabajo.getCantidadTrabajada());

    //TODO: Para agregar el comentario agregar un DTO que tenga la cantidad y el comentario
    //trabajo.setComentario(dto.comentario());

    //TODO: Luego que el FE valide si ya termino el trabajo y avance al siguiente estado, solo hay que validar si el Pegado se asgina aqui
    // o que lo asigne el FE automaticamente al avanzer el estado. Entonces seria: Avanzar a Pegado y luego asignar el trabajo de pegado, a la inversa de la reparacion.
    // Aunque tal vez se podria hacer lo mismo con la reparacion para mantener consitencia.

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
  
  // Asigna trabajo
  @PostMapping("/asignar-trabajo/{idOrden}/{idOrdenDetalle}/{trabajador}")
  public OrdenTrabajo asignarTrabajo(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @PathVariable String trabajador) {

    // Obtener fecha agendada de la orden
    OrdenCalendario ordenCalendario = ordenCalendarioRepository.findById(idOrden)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La orden no se encuentra agendada en el calendario"));
            
    // Obtencion de detalle de orden
    OrdenDetalle detalleReparacion = ordenDetalleRepository.findById(new OrdenDetallePK(idOrden, idOrdenDetalle)).
          orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Detalle no encontrado"));

    // Obtencion de seguimiento del detalle
    OrdenSeguimiento ordenSeguimientoActual = ordenSeguimientoRepository.findById(new OrdenSeguimientoPK(idOrden, idOrdenDetalle)).
          orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El detalle no se encuentra en seguimiento"));
   
    // Obtenemos el siguiente estado
    ProductoTipoEstadoPK productoTipoEstadoSiguientePK = new ProductoTipoEstadoPK(ordenSeguimientoActual.getTipo(), ordenSeguimientoActual.getSubTipo(), ordenSeguimientoActual.getSecuencia() + 1);
    
    ProductoTipoEstado productoTipoEstadoSiguiente = productoTipoEstadoRepository.findById(productoTipoEstadoSiguientePK).
      orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Siguiente estado no encontrado"));

    //TODO: Es necesario Enmarcado?
    // tambien se podria validar con el estado actual: Repartir, Impresion pero este podria permitir otros estados como las Ampliaciones que pasan directo a Listo
    if (!List.of("Reparacion", "Normal", "Enmarcado", "Pegado").contains(productoTipoEstadoSiguiente.getEstado())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El siguiente estado del producto no es valido para asignar trabajo");
    }

    OrdenTrabajo trabajo = new OrdenTrabajo();

    if (productoTipoEstadoSiguiente.getEstado().equals("Reparacion")) {
      if (!usuarioRepository.usuarioEsReparador(trabajador)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no es un reparador");
      }
      trabajo.setRol("repara");
    }

    if (productoTipoEstadoSiguiente.getEstado().equals("Normal")) {
      if (!usuarioRepository.usuarioEsNormal(trabajador)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no es normal");
      }
      trabajo.setRol("normal");
    }

    if (productoTipoEstadoSiguiente.getEstado().equals("Pegado")) {
      //TODO: Por ahora, pasar "Pegador" desde el FE
      if (!usuarioRepository.usuarioEsPegador(trabajador)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no es pega");
      }
      ProductoTipoEstadoPK productoTipoEstadoAnteriorPK = new ProductoTipoEstadoPK(ordenSeguimientoActual.getTipo(), ordenSeguimientoActual.getSubTipo(), ordenSeguimientoActual.getSecuencia() - 1);

      ProductoTipoEstado productoTipoEstadoAnterior = productoTipoEstadoRepository.findById(productoTipoEstadoAnteriorPK).
        orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado anterior no encontrado para asignar pegado"));

      OrdenTrabajoPK pk = new OrdenTrabajoPK(idOrden, idOrdenDetalle, productoTipoEstadoAnterior.getEstado());

      OrdenTrabajo trabajoNormalReparacion = ordenTrabajoRepository.findById(pk)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay trabajo asignado para este estado. No se puede asignar a pegado"));

      // Para el pegado se le asigna la cantidad trabajada en el estado anterior, que equivale a la cantidad impresa.
      trabajo.setRol("pega");
      trabajo.setCantidadAsignada(trabajoNormalReparacion.getCantidadTrabajada());
      trabajo.setCantidadTrabajada(0);
      trabajo.setCantidadNoTrabajada(trabajoNormalReparacion.getCantidadTrabajada());
    } else {
      // Por ahora le ponomes la cantidad del detalle.
      trabajo.setCantidadAsignada(detalleReparacion.getCantidad());
      // Inicialmente, toda la cantidad esta sin trabajar, a medida que vaya avanzando en el trabajo, se iran actualizando estos campos
      trabajo.setCantidadTrabajada(0);
      trabajo.setCantidadNoTrabajada(detalleReparacion.getCantidad());
    }

    trabajo.setIdOrden(idOrden);
    trabajo.setIdOrdenDetalle(idOrdenDetalle);
    trabajo.setEstado(productoTipoEstadoSiguiente.getEstado());
    trabajo.setSecuencia(productoTipoEstadoSiguiente.getSecuencia());
    trabajo.setTrabajador(trabajador);
    trabajo.setIdProducto(detalleReparacion.getIdProducto());
    

    trabajo.setComentario("");
    // Misma fecha de la orden
    trabajo.setFechaTrabajo(ordenCalendario.getFecha());

    // pendiente por ahora
    //trabajo.setIdSeguimiento();

    return ordenTrabajoRepository.save(trabajo);

  }
}