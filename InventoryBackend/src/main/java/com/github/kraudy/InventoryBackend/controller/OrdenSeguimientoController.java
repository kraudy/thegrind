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
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetalleFacturacionDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetalleEntregaDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetalleImpresionDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetallePreparacionDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoEstadosGeneralDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoFacturacionDTO;
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
import com.github.kraudy.InventoryBackend.service.NotificationService;

import com.github.kraudy.InventoryBackend.service.OrdenSeguimientoService;
import lombok.RequiredArgsConstructor;

import jakarta.websocket.server.PathParam;

import java.time.Duration;

@RestController
@RequestMapping("/api/ordenes-seguimiento")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
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

  // inject the service
  @Autowired
  private NotificationService notificationService;

  private final OrdenSeguimientoService ordenSeguimientoService;

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
  @GetMapping("/general")
  public List<OrdenSeguimientoEstadosGeneralDTO> getOrdenesSeguimientoGeneral(
          @RequestParam(required = false) String search,
          @RequestParam(required = false) String estadoOrden) {
    return ordenSeguimientoRepository.getOrdenesPorEstadosSeguimiento(search, estadoOrden);
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


  /* Retorna lista de ordenes para facturacion */
  @GetMapping("/para-facturacion")
  public List<OrdenSeguimientoFacturacionDTO> getOrdenesParaFacturacion(
    @RequestParam(required = false) Long id,
    @RequestParam(required = false) String cliente,
    @RequestParam(required = false) String trabajador
  ) {
    return ordenSeguimientoRepository.getOrdenesParaFacturacion(id, cliente, trabajador);
  }

  @GetMapping("/para-facturacion/{idOrden}")
  public List<OrdenSeguimientoDetalleFacturacionDTO> getSeguimientoDeOrdenParaFacturacion(@PathVariable Long idOrden) {
    return ordenSeguimientoRepository.getSeguimientoDeOrdenParaFacturacion(idOrden);
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
  public OrdenSeguimiento reverseState(@PathVariable Long idOrden, @PathVariable Long idOrdenDetalle) {
      return ordenSeguimientoService.reverseState(idOrden, idOrdenDetalle);
  }

  // Avanzar detalle al siguiente estado
  @PostMapping("/avanzar/{idOrden}/{idOrdenDetalle}")
  public OrdenSeguimiento advanceState(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle) {

    return ordenSeguimientoService.advanceState(idOrden, idOrdenDetalle);
  }

}
