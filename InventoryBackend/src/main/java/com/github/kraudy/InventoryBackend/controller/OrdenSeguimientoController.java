package com.github.kraudy.InventoryBackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoPK;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDTO;
import com.github.kraudy.InventoryBackend.model.Orden;
import com.github.kraudy.InventoryBackend.model.OrdenDetalle;
import com.github.kraudy.InventoryBackend.model.OrdenDetallePK;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimiento;
import com.github.kraudy.InventoryBackend.model.ProductoTipo;
import com.github.kraudy.InventoryBackend.model.ProductoTipoEstado;
import com.github.kraudy.InventoryBackend.repository.OrdenDetalleRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenSeguimientoRepository;
import com.github.kraudy.InventoryBackend.repository.ProductoTipoEstadoRepository;

@RestController
@RequestMapping("/api/ordenes-seguimiento")
@CrossOrigin(origins = "http://localhost:4200")
public class OrdenSeguimientoController {

  @Autowired
  private OrdenSeguimientoRepository ordenSeguimientoRepository;

  @Autowired
  private ProductoTipoEstadoRepository productoTipoEstadoRepository;

  @Autowired
  private OrdenDetalleRepository ordenDetalleRepository;

  @Autowired
  private OrdenRepository ordenRepository;

  @GetMapping
  public List<OrdenSeguimiento> getAll() {
    return ordenSeguimientoRepository.findAll();
  }

  @GetMapping("/{idOrden}/{idOrdenDetalle}/{idProducto}/{estado}")
  public OrdenSeguimiento getById(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @PathVariable Long idProducto,
          @PathVariable String estado) {

    OrdenSeguimientoPK pk = new OrdenSeguimientoPK(idOrden, idOrdenDetalle, idProducto, estado);
    return ordenSeguimientoRepository.findById(pk).orElse(null);
  }

  @PostMapping
  public OrdenSeguimiento create(
      @PathVariable Long idOrden,
      @PathVariable Long idOrdenDetalle,
      @PathVariable Long idProducto,
      @PathVariable String estado) {

    if (idOrden == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de la orden es obligatorio");
    }
    if (idProducto == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del producto es obligatorio");
    }

    OrdenSeguimiento ordenSeguimiento = new OrdenSeguimiento();

    ordenSeguimiento.setIdOrden(idOrden);
    ordenSeguimiento.setIdOrdenDetalle(idOrdenDetalle);
    ordenSeguimiento.setIdProducto(idProducto);
    ordenSeguimiento.setEstado(estado);
    
    return ordenSeguimientoRepository.save(ordenSeguimiento);
  }

  // I think this is not needed here
  //@PutMapping("/{idOrden}/{idOrdenDetalle}/{idProducto}/{estado}")
  //public OrdenSeguimiento update(@PathVariable Long idOrden,
  //    @PathVariable Long idOrdenDetalle,
  //    @PathVariable Long idProducto,
  //    @PathVariable String estado) {

  //  OrdenSeguimientoPK pk = new OrdenSeguimientoPK(idOrden, idOrdenDetalle, idProducto, estado);

  //  OrdenSeguimiento ordenSeguimiento = ordenSeguimientoRepository.findById(pk)
  //          .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Detalle seguimeinto de orden no encontrado"));

  //  ordenSeguimiento.setEstado(estado);

  //  return ordenSeguimientoRepository.save(ordenSeguimiento);
  //}

  @DeleteMapping("/{idOrden}/{idOrdenDetalle}/{idProducto}/{estado}")
  public void delete(@PathVariable Long idOrden,
      @PathVariable Long idOrdenDetalle,
      @PathVariable Long idProducto,
      @PathVariable String estado) {

    OrdenSeguimientoPK pk = new OrdenSeguimientoPK(idOrden, idOrdenDetalle, idProducto, estado);

    ordenSeguimientoRepository.deleteById(pk);
  }

  @GetMapping("/orden/{idOrden}")
  public List<OrdenSeguimientoDTO> getFullSeguimiento(@PathVariable Long idOrden) {
      return ordenSeguimientoRepository.getFullSeguimientoByOrden(idOrden);
  }

  // 1. Estados posibles para un producto
  @GetMapping("/posibles/{tipo}/{subTipo}")
  public List<ProductoTipoEstado> getPossibleStates(
          @PathVariable String tipo, 
          @PathVariable String subTipo) {
    return productoTipoEstadoRepository.findByTipoAndSubTipoOrderBySecuenciaAsc(tipo, subTipo);
  }

  // 2. Historial de seguimiento de un detalle
  @GetMapping("/por-detalle/{idOrden}/{idOrdenDetalle}/{idProducto}")
  public List<OrdenSeguimiento> getByDetalle(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @PathVariable Long idProducto) {
      return ordenSeguimientoRepository.findByDetalleOrderByFechaCreacionAsc(idOrden, idOrdenDetalle, idProducto);
  }

  // 3. Avanzar al siguiente estado (la magia está aquí)
  @PostMapping("/avanzar/{idOrden}/{idOrdenDetalle}/{idProducto}")
  public OrdenSeguimiento advanceState(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @PathVariable Long idProducto) {

      // Obtener tipo/subTipo del producto
      OrdenDetalle detalle = ordenDetalleRepository.findById(new OrdenDetallePK(idOrden, idOrdenDetalle, idProducto))
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Detalle no encontrado"));

      String tipo = detalle.getProducto().getTipoProducto();
      String subTipo = detalle.getProducto().getSubTipoProducto();

      List<ProductoTipoEstado> workflow = productoTipoEstadoRepository.findByTipoAndSubTipoOrderBySecuenciaAsc(tipo, subTipo);
      if (workflow.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay workflow para este producto");

      // Estado actual (el más reciente)
      List<OrdenSeguimiento> history = ordenSeguimientoRepository.findByDetalleOrderByFechaCreacionDesc(idOrden, idOrdenDetalle, idProducto);
      int currentSec = history.isEmpty() ? 0 : workflow.stream()
              .filter(s -> s.getEstado().equals(history.get(0).getEstado()))
              .findFirst().map(ProductoTipoEstado::getSecuencia).orElse(0);

      int nextSec = currentSec + 1;
      if (nextSec > workflow.size()) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya está en el último estado");
      }

      ProductoTipoEstado nextState = workflow.get(nextSec - 1);

      OrdenSeguimiento nuevo = new OrdenSeguimiento();
      nuevo.setIdOrden(idOrden);
      nuevo.setIdOrdenDetalle(idOrdenDetalle);
      nuevo.setIdProducto(idProducto);
      nuevo.setEstado(nextState.getEstado());
      nuevo.setSeguimientoPor("adminTest");   // ← cambia por usuario real del login

      OrdenSeguimiento saved = ordenSeguimientoRepository.save(nuevo);

      // Si todos los detalles llegaron al final → orden lista
      checkAndMarkOrderAsReady(idOrden);

      return saved;
  }

  private void checkAndMarkOrderAsReady(Long idOrden) {
    List<OrdenDetalle> detalles = ordenDetalleRepository.findByIdOrden(idOrden);
    boolean allDone = true;

    for (OrdenDetalle det : detalles) {
      String t = det.getProducto().getTipoProducto();
      String st = det.getProducto().getSubTipoProducto();
      List<ProductoTipoEstado> wf = productoTipoEstadoRepository.findByTipoAndSubTipoOrderBySecuenciaAsc(t, st);
      int maxSeq = wf.size();

      List<OrdenSeguimiento> hist = ordenSeguimientoRepository.findByDetalleOrderByFechaCreacionDesc(
              idOrden, det.getIdOrdenDetalle(), det.getIdProducto());

      int lastSeq = hist.isEmpty() ? 0 : wf.stream()
              .filter(s -> s.getEstado().equals(hist.get(0).getEstado()))
              .findFirst().map(ProductoTipoEstado::getSecuencia).orElse(0);

      if (lastSeq < maxSeq) {
          allDone = false;
          break;
      }
    }

    if (allDone) {
      Orden order = ordenRepository.findById(idOrden).orElseThrow();
      order.setEstado("Lista");   // o "Preparada" según tu flujo
      ordenRepository.save(order);
    }
  }
}
