package com.github.kraudy.InventoryBackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoPK;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetalleDTO;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimiento;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoHistorico;
import com.github.kraudy.InventoryBackend.model.ProductoTipoEstado;
import com.github.kraudy.InventoryBackend.model.ProductoTipoEstadoPK;
import com.github.kraudy.InventoryBackend.repository.OrdenSeguimientoHistoricoRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenSeguimientoRepository;
import com.github.kraudy.InventoryBackend.repository.ProductoTipoEstadoRepository;

import jakarta.websocket.server.PathParam;

import java.time.Duration;

@RestController
@RequestMapping("/api/ordenes-seguimiento")
@CrossOrigin(origins = "http://localhost:4200")
public class OrdenSeguimientoController {

  @Autowired
  private OrdenSeguimientoRepository ordenSeguimientoRepository;

  @Autowired
  private ProductoTipoEstadoRepository productoTipoEstadoRepository;

  @Autowired
  private OrdenSeguimientoHistoricoRepository ordenSeguimientoHistoricoRepository;

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

  /* Retorna lista de ordenes con detalle en estados de espera de impresion */
  @GetMapping("/para-impresion")
  public List<OrdenSeguimientoDTO> getOrdenesParaImpresion() {
    return ordenSeguimientoRepository.getOrdenesParaImpresion();
  }

  @GetMapping("/para-impresion/{idOrden}")
  public List<OrdenSeguimientoDetalleDTO> getSeguimientoDeOrdenParaImpresion(@PathVariable Long idOrden) {
    return ordenSeguimientoRepository.getSeguimientoDeOrdenParaImpresion(idOrden);
  }

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

  // 2. seguimiento de un detalle
  @GetMapping("/por-detalle/{idOrden}/{idOrdenDetalle}")
  public List<OrdenSeguimiento> getByDetalle(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle) {
      return ordenSeguimientoRepository.findByDetalleOrderByFechaCreacionAsc(idOrden, idOrdenDetalle);
  }

  // @PutMapping?
  // 3. Avanzar al siguiente estado (la magia está aquí)
  @PostMapping("/avanzar/{idOrden}/{idOrdenDetalle}")
  public OrdenSeguimiento advanceState(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle) {

    OrdenSeguimientoPK ordenSeguimientoPK = new OrdenSeguimientoPK(idOrden, idOrdenDetalle);

    OrdenSeguimiento ordenSeguimientoActual = ordenSeguimientoRepository.findById(ordenSeguimientoPK).
      orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El detalle no se encuentra en seguimiento"));

    // Se le suma uno para obtener el siguiente estado
    ProductoTipoEstadoPK productoTipoEstadoPK = new ProductoTipoEstadoPK(ordenSeguimientoActual.getTipo(), ordenSeguimientoActual.getSubTipo(), ordenSeguimientoActual.getSecuencia() + 1);
    
    ProductoTipoEstado productoTipoEstado = productoTipoEstadoRepository.findById(productoTipoEstadoPK).
      orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Siguiente estado no encontrado"));

    OrdenSeguimientoHistorico historico = new OrdenSeguimientoHistorico();
    historico.setIdOrden(ordenSeguimientoActual.getIdOrden());
    historico.setIdOrdenDetalle(ordenSeguimientoActual.getIdOrdenDetalle());
    historico.setEstado(ordenSeguimientoActual.getEstado());

    historico.setFechaCreacion(ordenSeguimientoActual.getFechaModificacion()); // Utilizamos la fecha de modificacion
    historico.setUsuarioCreacion(ordenSeguimientoActual.getSeguimientoPor());

    ordenSeguimientoActual.setSeguimientoPor("adminTestfinaliza");
    // Se actualiza estado nuevo, secuencia y usuario que finaliza estado previo
    ordenSeguimientoActual.setEstado(productoTipoEstado.getEstado());
    ordenSeguimientoActual.setSecuencia(productoTipoEstado.getSecuencia());

    // Actualizamos el estado actual
    ordenSeguimientoActual = ordenSeguimientoRepository.save(ordenSeguimientoActual);

    // Agregamos datos pendientes al historico
    historico.setFechaFinalizacion(ordenSeguimientoActual.getFechaModificacion());
    historico.setUsuarioFinalizacion(ordenSeguimientoActual.getSeguimientoPor());

    historico.setDuracion(Duration.between(historico.getFechaCreacion(), historico.getFechaFinalizacion()).toHours());

    // Guardamos el historico
    ordenSeguimientoHistoricoRepository.save(historico);

    // Si todos los detalles llegaron al final → orden lista
    //TODO: Just add one to secuencia and do the get, then check if there is another and finish
    //checkAndMarkOrderAsReady(idOrden);

    return ordenSeguimientoActual;
  }

}
