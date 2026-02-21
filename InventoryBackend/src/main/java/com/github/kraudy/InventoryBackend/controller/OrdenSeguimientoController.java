package com.github.kraudy.InventoryBackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoPK;
import com.github.kraudy.InventoryBackend.model.OrdenDetalle;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimiento;
import com.github.kraudy.InventoryBackend.model.ProductoTipo;
import com.github.kraudy.InventoryBackend.repository.OrdenSeguimientoRepository;

@RestController
@RequestMapping("/api/ordenes-seguimiento")
@CrossOrigin(origins = "http://localhost:4200")
public class OrdenSeguimientoController {

  @Autowired
  private OrdenSeguimientoRepository ordenSeguimientoRepository;

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
}
