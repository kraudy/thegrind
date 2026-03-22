package com.github.kraudy.InventoryBackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoPK;
import com.github.kraudy.InventoryBackend.model.OrdenTrabajoPK;
import com.github.kraudy.InventoryBackend.dto.EstadosPorDetalleDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetalleDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetalleEntregaDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetalleImpresionDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetallePreparacionDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoEstadosDTO;
import com.github.kraudy.InventoryBackend.model.Orden;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimiento;
import com.github.kraudy.InventoryBackend.model.OrdenTrabajo;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoHistorico;
import com.github.kraudy.InventoryBackend.model.ProductoTipoEstado;
import com.github.kraudy.InventoryBackend.model.ProductoTipoEstadoPK;
import com.github.kraudy.InventoryBackend.repository.OrdenSeguimientoHistoricoRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenSeguimientoRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenTrabajoRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenRepository;
import com.github.kraudy.InventoryBackend.repository.ProductoTipoEstadoRepository;

import jakarta.websocket.server.PathParam;

import java.time.Duration;

@RestController
@RequestMapping("/api/ordenes-seguimiento")
@CrossOrigin(origins = "http://localhost:4200")
public class OrdenSeguimientoController {

  @Autowired
  private OrdenRepository ordenRepository;

  @Autowired
  private OrdenSeguimientoRepository ordenSeguimientoRepository;

  @Autowired
  private ProductoTipoEstadoRepository productoTipoEstadoRepository;

  @Autowired
  private OrdenSeguimientoHistoricoRepository ordenSeguimientoHistoricoRepository;

  @Autowired
  private OrdenTrabajoRepository ordenTrabajoRepository;

  @GetMapping
  public List<OrdenSeguimiento> getAll() {
    return ordenSeguimientoRepository.findAll();
  }

  @GetMapping("/{idOrden}/{idOrdenDetalle}")
  public OrdenSeguimiento getById(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle) {

    OrdenSeguimientoPK pk = new OrdenSeguimientoPK(idOrden, idOrdenDetalle);
    return ordenSeguimientoRepository.findById(pk).orElse(null);
  }

  @DeleteMapping("/{idOrden}/{idOrdenDetalle}")
  public void delete(@PathVariable Long idOrden,
      @PathVariable Long idOrdenDetalle) {

    OrdenSeguimientoPK pk = new OrdenSeguimientoPK(idOrden, idOrdenDetalle);

    ordenSeguimientoRepository.deleteById(pk);
  }

  /* Retorna lista de ordenes de hoy con detalles en estados de seguimiento para vista de seguimiento general */
  @GetMapping("/por-estados")
  public List<OrdenSeguimientoEstadosDTO> getOrdenesPorEstadosSeguimiento() {
    return ordenSeguimientoRepository.getOrdenesPorEstadosSeguimiento();
  }

  /* Retorna lista de ordenes de hoy con detalle en estados de espera de impresion */
  @GetMapping("/para-impresion")
  public List<OrdenSeguimientoDTO> getOrdenesParaImpresion() {
    return ordenSeguimientoRepository.getOrdenesParaImpresion();
  }

  @GetMapping("/para-impresion/{idOrden}")
  public List<OrdenSeguimientoDetalleImpresionDTO> getSeguimientoDeOrdenParaImpresion(@PathVariable Long idOrden) {
    return ordenSeguimientoRepository.getSeguimientoDeOrdenParaImpresion(idOrden);
  }

  /* Retorna lista de ordenes de hoy con detalle en estados de espera de preparacion */
  @GetMapping("/para-preparacion")
  public List<OrdenSeguimientoDTO> getOrdenesParaPreparacion() {
    return ordenSeguimientoRepository.getOrdenesParaPreparacion();
  }

  @GetMapping("/para-preparacion/{idOrden}")
  public List<OrdenSeguimientoDetallePreparacionDTO> getSeguimientoDeOrdenParaPreparacion(@PathVariable Long idOrden) {
    return ordenSeguimientoRepository.getSeguimientoDeOrdenParaPreparacion(idOrden);
  }

  /* Retorna lista de ordenes con detalle en estados de espera de entrega */
  @GetMapping("/para-entrega")
  public List<OrdenSeguimientoDTO> getOrdenesParaEntrega() {
    return ordenSeguimientoRepository.getOrdenesParaEntrega();
  }

  @GetMapping("/para-entrega/{idOrden}")
  public List<OrdenSeguimientoDetalleEntregaDTO> getSeguimientoDeOrdenParaEntrega(@PathVariable Long idOrden) {
    return ordenSeguimientoRepository.getSeguimientoDeOrdenParaEntrega(idOrden);
  }

  /* Retorna lista de ordenes para repartir */
  @GetMapping("/para-repartir")
  public List<OrdenSeguimientoDTO> getOrdenesParaRepartir() {
    return ordenSeguimientoRepository.getOrdenesParaRepartir();
  }

  @GetMapping("/para-repartir/{idOrden}")
  public List<OrdenSeguimientoDetalleDTO> getSeguimientoDeOrdenParaRepartir(@PathVariable Long idOrden) {
    return ordenSeguimientoRepository.getSeguimientoDeOrdenParaRepartir(idOrden);
  }

  /* Muestra el seguimiento completo de los detalles de una orden */
  @GetMapping("/orden/{idOrden}")
  public List<OrdenSeguimientoDetalleDTO> getFullSeguimiento(@PathVariable Long idOrden) {
    return ordenSeguimientoRepository.getFullSeguimientoByOrden(idOrden);
  }

  // 1. Estados posibles para un producto
  @GetMapping("/posibles/{tipo}/{subTipo}")
  public List<ProductoTipoEstado> getPossibleStates(
          @PathVariable String tipo, 
          @PathVariable String subTipo) {
    return productoTipoEstadoRepository.findByTipoAndSubTipoOrderBySecuenciaAsc(tipo, subTipo);
  }

  @GetMapping("/estados-por-detalle/{idOrden}")
  public List<EstadosPorDetalleDTO> getEstadosPorDetalle(@PathVariable Long idOrden) {
      return ordenSeguimientoRepository.getEstadosPorDetalle(idOrden);
  }

  // 2. seguimiento de un detalle
  @GetMapping("/por-detalle/{idOrden}/{idOrdenDetalle}")
  public List<OrdenSeguimiento> getByDetalle(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle) {
      return ordenSeguimientoRepository.findByDetalleOrderByFechaCreacionAsc(idOrden, idOrdenDetalle);
  }

  /* Movimientos de estados */

  // Regresar detalle al estado anterior
  @PostMapping("/regresar/{idOrden}/{idOrdenDetalle}")
  public OrdenSeguimiento reverseState(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle) {

    OrdenSeguimientoPK ordenSeguimientoPK = new OrdenSeguimientoPK(idOrden, idOrdenDetalle);

    OrdenSeguimiento ordenSeguimientoActual = ordenSeguimientoRepository.findById(ordenSeguimientoPK).
      orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El detalle no se encuentra en seguimiento"));

    // Verificar si hay un estado anterior (secuencia - 1)
    if (ordenSeguimientoActual.getSecuencia() <= 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay estado anterior disponible");
    }

    // Se le resta uno para obtener el estado anterior
    ProductoTipoEstadoPK productoTipoEstadoPK = new ProductoTipoEstadoPK(ordenSeguimientoActual.getTipo(), ordenSeguimientoActual.getSubTipo(), ordenSeguimientoActual.getSecuencia() - 1);

    ProductoTipoEstado productoTipoEstadoPrevio = productoTipoEstadoRepository.findById(productoTipoEstadoPK).
      orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado anterior no encontrado"));

    // Si el estado era listo, se marca orden como repartida otra vez
    if (ordenSeguimientoActual.getEstado().equals("Listo")) {
      // Se marca orden como repartida
      ordenRepository.updateEstado(ordenSeguimientoActual.getIdOrden(), "Repartida");
    }

    // Si el estado era Reparacion, Normal, Enmarcado, Pegado, se elimina orden de trabajo asociada
    if (List.of("Reparacion", "Normal").contains(ordenSeguimientoActual.getEstado())) {
      OrdenTrabajoPK ordenTrabajoPK = new OrdenTrabajoPK(ordenSeguimientoActual.getIdOrden(), ordenSeguimientoActual.getIdOrdenDetalle(), ordenSeguimientoActual.getEstado());
      ordenTrabajoRepository.deleteById(ordenTrabajoPK);
    }

    if (List.of("Impresion").contains(ordenSeguimientoActual.getEstado())) {
      OrdenTrabajoPK ordenTrabajoPK = new OrdenTrabajoPK(ordenSeguimientoActual.getIdOrden(), ordenSeguimientoActual.getIdOrdenDetalle(), ordenSeguimientoActual.getEstado());
      ordenTrabajoRepository.deleteById(ordenTrabajoPK);
    }

    // Si el estado es pegado, se elimina el trabajo asignado que corresponde a lo que se imprimio
    if (List.of("Pegado").contains(ordenSeguimientoActual.getEstado())) {
      OrdenTrabajoPK ordenTrabajoPK = new OrdenTrabajoPK(ordenSeguimientoActual.getIdOrden(), ordenSeguimientoActual.getIdOrdenDetalle(), ordenSeguimientoActual.getEstado());
      ordenTrabajoRepository.deleteById(ordenTrabajoPK);
    }
    
    if (List.of("Enmarcado").contains(ordenSeguimientoActual.getEstado())) {
      OrdenTrabajoPK ordenTrabajoPK = new OrdenTrabajoPK(ordenSeguimientoActual.getIdOrden(), ordenSeguimientoActual.getIdOrdenDetalle(), ordenSeguimientoActual.getEstado());
      ordenTrabajoRepository.deleteById(ordenTrabajoPK);
    }

    // Elimina el trabajo asignado a "Entregado"
    if (List.of("Listo").contains(ordenSeguimientoActual.getEstado())) {
      ProductoTipoEstadoPK productoTipoEstadoSiguientePK = new ProductoTipoEstadoPK(ordenSeguimientoActual.getTipo(), ordenSeguimientoActual.getSubTipo(), ordenSeguimientoActual.getSecuencia() + 1);

      ProductoTipoEstado productoTipoEstadoSiguiente = productoTipoEstadoRepository.findById(productoTipoEstadoSiguientePK).
        orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado siguiente a Listo no encontrado"));

      OrdenTrabajoPK ordenTrabajoPK = new OrdenTrabajoPK(ordenSeguimientoActual.getIdOrden(), ordenSeguimientoActual.getIdOrdenDetalle(), productoTipoEstadoSiguiente.getEstado());
      ordenTrabajoRepository.deleteById(ordenTrabajoPK);
    }

    /* La logica aqui es que si estaba en Enmarcado o Pegado, se debe resetear el trabajo realizado del estado de reparacion o normal previo proque se tienen que imprimir otra vez */
    // Si la orden esta en estado Enmarcado o Pegado, se resetea el trabajo realizado del estado de reparacion o normal previo, para que vuelva a aparecer como tarea pendiente para el reparador
    if (List.of("Impresion", "Enmarcado", "Pegado", "Listo", "Entregado").contains(ordenSeguimientoActual.getEstado())) {
      ProductoTipoEstadoPK productoTipoEstadoNormalReparacionPK = null;
      // Aqui lo tengo que filtrar por el tipo debido a que las ampliaciones cuando estan listas se les debe restar 2
      if (List.of("Impresion").contains(ordenSeguimientoActual.getEstado()) || (List.of("Listo").contains(ordenSeguimientoActual.getEstado()) && List.of("Retablos", "Molduras").contains(ordenSeguimientoActual.getTipo()))) {
        // Para las impresion la orden quedo en estado Normal o Reparacion por eso se le resta 1 a la secuencia
        // Igual para la lista porque el estado anterior puede ser Pegado, Enmarcado o Normal, Reparacion.
        productoTipoEstadoNormalReparacionPK = new ProductoTipoEstadoPK(ordenSeguimientoActual.getTipo(), ordenSeguimientoActual.getSubTipo(), ordenSeguimientoActual.getSecuencia() - 1); 
      } else if (List.of("Entregado").contains(ordenSeguimientoActual.getEstado())) {
        // Estado actual
        productoTipoEstadoNormalReparacionPK = new ProductoTipoEstadoPK(ordenSeguimientoActual.getTipo(), ordenSeguimientoActual.getSubTipo(), ordenSeguimientoActual.getSecuencia()); 
      }
        else {
        productoTipoEstadoNormalReparacionPK = new ProductoTipoEstadoPK(ordenSeguimientoActual.getTipo(), ordenSeguimientoActual.getSubTipo(), ordenSeguimientoActual.getSecuencia() - 2);
      }

      ProductoTipoEstado productoTipoEstadoNormalReparacion = productoTipoEstadoRepository.findById(productoTipoEstadoNormalReparacionPK).
        orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado anterior no encontrado"));

      OrdenTrabajoPK ordenTrabajoPK = new OrdenTrabajoPK(ordenSeguimientoActual.getIdOrden(), ordenSeguimientoActual.getIdOrdenDetalle(), productoTipoEstadoNormalReparacion.getEstado());
      OrdenTrabajo trabajo = ordenTrabajoRepository.findById(ordenTrabajoPK)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay trabajo asignado para este estado"));
      // Se resetea trabajo realizado
      trabajo.setCantidadTrabajada(0);
      trabajo.setCantidadNoTrabajada(trabajo.getCantidadAsignada());
      ordenTrabajoRepository.save(trabajo);
    }

    OrdenSeguimientoHistorico historico = new OrdenSeguimientoHistorico();
    historico.setIdOrden(ordenSeguimientoActual.getIdOrden());
    historico.setIdOrdenDetalle(ordenSeguimientoActual.getIdOrdenDetalle());
    historico.setEstado(ordenSeguimientoActual.getEstado());

    historico.setFechaCreacion(ordenSeguimientoActual.getFechaModificacion()); // Utilizamos la fecha de modificacion
    historico.setUsuarioCreacion(ordenSeguimientoActual.getSeguimientoPor());

    ordenSeguimientoActual.setSeguimientoPor("adminTestregresa");
    // Se actualiza estado previo, secuencia y usuario que finaliza estado previo
    ordenSeguimientoActual.setEstado(productoTipoEstadoPrevio.getEstado());
    ordenSeguimientoActual.setSecuencia(productoTipoEstadoPrevio.getSecuencia());

    // Actualizamos el estado actual
    ordenSeguimientoActual = ordenSeguimientoRepository.save(ordenSeguimientoActual);

    // Agregamos datos pendientes al historico
    historico.setFechaFinalizacion(ordenSeguimientoActual.getFechaModificacion());
    historico.setUsuarioFinalizacion(ordenSeguimientoActual.getSeguimientoPor());

    historico.setDuracion(Duration.between(historico.getFechaCreacion(), historico.getFechaFinalizacion()).toMinutes());



    // Guardamos el historico
    ordenSeguimientoHistoricoRepository.save(historico);

    return ordenSeguimientoActual;
  }

  // Avanzar detalle al siguiente estado
  @PostMapping("/avanzar/{idOrden}/{idOrdenDetalle}")
  public OrdenSeguimiento advanceState(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle) {

    OrdenSeguimientoPK ordenSeguimientoPK = new OrdenSeguimientoPK(idOrden, idOrdenDetalle);

    OrdenSeguimiento ordenSeguimientoActual = ordenSeguimientoRepository.findById(ordenSeguimientoPK).
      orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El detalle no se encuentra en seguimiento"));

    // Se le suma uno para obtener el siguiente estado
    ProductoTipoEstadoPK productoTipoEstadoPK = new ProductoTipoEstadoPK(ordenSeguimientoActual.getTipo(), ordenSeguimientoActual.getSubTipo(), ordenSeguimientoActual.getSecuencia() + 1);
    
    ProductoTipoEstado productoTipoEstadoSiguiente = productoTipoEstadoRepository.findById(productoTipoEstadoPK).
      orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Siguiente estado no encontrado"));

    OrdenSeguimientoHistorico historico = new OrdenSeguimientoHistorico();
    historico.setIdOrden(ordenSeguimientoActual.getIdOrden());
    historico.setIdOrdenDetalle(ordenSeguimientoActual.getIdOrdenDetalle());
    historico.setEstado(ordenSeguimientoActual.getEstado());

    historico.setFechaCreacion(ordenSeguimientoActual.getFechaModificacion()); // Utilizamos la fecha de modificacion
    historico.setUsuarioCreacion(ordenSeguimientoActual.getSeguimientoPor());

    ordenSeguimientoActual.setSeguimientoPor("adminTestfinaliza");
    // Se actualiza estado nuevo, secuencia y usuario que finaliza estado previo
    ordenSeguimientoActual.setEstado(productoTipoEstadoSiguiente.getEstado());
    ordenSeguimientoActual.setSecuencia(productoTipoEstadoSiguiente.getSecuencia());

    // Actualizamos el estado actual
    ordenSeguimientoActual = ordenSeguimientoRepository.save(ordenSeguimientoActual);

    // Agregamos datos pendientes al historico
    historico.setFechaFinalizacion(ordenSeguimientoActual.getFechaModificacion());
    historico.setUsuarioFinalizacion(ordenSeguimientoActual.getSeguimientoPor());

    historico.setDuracion(Duration.between(historico.getFechaCreacion(), historico.getFechaFinalizacion()).toMinutes());

    // Guardamos el historico
    ordenSeguimientoHistoricoRepository.save(historico);

    // Validamos si se esta asignando el trabajo antes de avanzar a reparacion
    if (productoTipoEstadoSiguiente.getEstado().equals("Reparacion")) {
      if (!ordenTrabajoRepository.detalleEstaAsignado(ordenSeguimientoActual.getIdOrden(), ordenSeguimientoActual.getIdOrdenDetalle(), productoTipoEstadoSiguiente.getEstado())){
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede avanzar a reparacion sin asignar detalle a reparador");
      }
    }

    // Pegado es diferente porque actualmente se le asigan todo lo que se imprime y segun esto se le paga. El alistado sera una validacion no restrictiva.
    if (productoTipoEstadoSiguiente.getEstado().equals("Pegado")) {
      if (!ordenTrabajoRepository.detalleEstaAsignado(ordenSeguimientoActual.getIdOrden(), ordenSeguimientoActual.getIdOrdenDetalle(), productoTipoEstadoSiguiente.getEstado())){
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede avanzar a pegado sin asignar detalle a pegador");
      }
    }

    // Validamos si todos los detalles estan listos para marcar orden como lista
    if (productoTipoEstadoSiguiente.getEstado().equals("Listo")) {
      if (ordenSeguimientoRepository.estanTodosLosDetallesListos(ordenSeguimientoActual.getIdOrden())){
        // Se marca orden como lista
        ordenRepository.updateEstado(ordenSeguimientoActual.getIdOrden(), productoTipoEstadoSiguiente.getEstado());
      }
      //if (ordenSeguimientoActual.getTipo().equals("Retablos")){
      //  //TODO:
      //}
    }

    // Validamos si todos los detalles estan entregados para marcar orden como entregada
    if (productoTipoEstadoSiguiente.getEstado().equals("Entregado")) {
      if (ordenSeguimientoRepository.estanTodosLosDetallesEntregados(ordenSeguimientoActual.getIdOrden())){
        // Se marca orden como entregada
        ordenRepository.updateEstado(ordenSeguimientoActual.getIdOrden(), productoTipoEstadoSiguiente.getEstado());
      }
    }

    // Si todos los detalles llegaron al final → orden lista
    //TODO: Just add one to secuencia and do the get, then check if there is another and finish
    //checkAndMarkOrderAsReady(idOrden);

    return ordenSeguimientoActual;
  }

}
