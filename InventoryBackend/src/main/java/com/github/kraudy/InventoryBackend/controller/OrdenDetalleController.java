package com.github.kraudy.InventoryBackend.controller;

import java.math.BigDecimal;

import com.github.kraudy.InventoryBackend.dto.OrdenDetalleDTO;
import com.github.kraudy.InventoryBackend.model.OrdenDetalle;
import com.github.kraudy.InventoryBackend.model.OrdenDetallePK;
import com.github.kraudy.InventoryBackend.model.ProductoPrecio;
import com.github.kraudy.InventoryBackend.model.ProductoPrecioPK;

import com.github.kraudy.InventoryBackend.repository.OrdenDetalleRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenRepository;
import com.github.kraudy.InventoryBackend.repository.ProductoPrecioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-detalle")
@CrossOrigin(origins = "http://localhost:4200")
public class OrdenDetalleController {

  @Autowired
  private OrdenRepository ordenRepository;

  @Autowired
  private OrdenDetalleRepository ordenDetalleRepository;

  @Autowired
  private ProductoPrecioRepository productoPrecioRepository;

  // Obtener todos los detalles de una orden específica (recomendado para uso frecuente)
  @GetMapping("/por-orden/{idOrden}")
  public List<OrdenDetalleDTO> getByOrden(@PathVariable Long idOrden) {
    return ordenDetalleRepository.getAllOrdenDetalle(idOrden);
  }

  // Obtener un detalle específico por clave compuesta
  @GetMapping("/{idOrden}/{idOrdenDetalle}")
  public OrdenDetalle getById(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle){

    OrdenDetallePK pk = new OrdenDetallePK(idOrden, idOrdenDetalle);
    return ordenDetalleRepository.findById(pk).orElse(null);
  }

  // Crear un nuevo detalle
  @PostMapping("/{idOrden}/{idProducto}")
  public void create(
      @PathVariable Long idOrden,
      @PathVariable Long idProducto,
      @RequestBody OrdenDetalle ordenDetalle) {

    if (idOrden == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de la orden es obligatorio");
    }
    if (idProducto == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del producto es obligatorio");
    }

    ProductoPrecioPK pkPrecio = new ProductoPrecioPK(idProducto, ordenDetalle.getPrecioUnitario());

    ProductoPrecio existingPrecio = productoPrecioRepository.findById(pkPrecio)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precio de producto no encontrado"));
    
    // De existir, validamos que la cantidad del detalle cumpla con el requisito mínimo del precio
    if (existingPrecio.getCantidadRequerida() > 0){
      if (ordenDetalle.getCantidad() <= existingPrecio.getCantidadRequerida()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para este precio, se necesita un minimo de " + existingPrecio.getCantidadRequerida() + " unidades del producto");
      }
    }

    ordenDetalle.setPrecioUnitario(existingPrecio.getPrecio());
    ordenDetalle.setSubtotal(existingPrecio.getPrecio().multiply(new BigDecimal(ordenDetalle.getCantidad())));

    ordenDetalleRepository.insertDetalle(idOrden, idProducto, ordenDetalle.getCantidad(), existingPrecio.getPrecio(), ordenDetalle.getSubtotal());

    // Actualizar totales de la orden después de crear detalle
    ordenRepository.updateOrdenTotales(idOrden);

    return;
  }

  // Actualizar un detalle existente
 @PutMapping("/{idOrden}/{idOrdenDetalle}")
  public OrdenDetalle update(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @RequestBody OrdenDetalle ordenDetalleActualizado) {

    // Construimos la PK para buscar la entidad existente
    OrdenDetallePK pk = new OrdenDetallePK(idOrden, idOrdenDetalle);

    OrdenDetalle existing = ordenDetalleRepository.findById(pk)
            .orElseThrow(() -> new RuntimeException("Detalle de orden no encontrado"));

    existing.setPrecioUnitario(ordenDetalleActualizado.getPrecioUnitario());
    existing.setCantidad(ordenDetalleActualizado.getCantidad());

    // PK de precio del producto para validar que exista el precio antes de actualizar el detalle
    ProductoPrecioPK pkPrecio = new ProductoPrecioPK(existing.getIdProducto(), existing.getPrecioUnitario());

    ProductoPrecio existingPrecio = productoPrecioRepository.findById(pkPrecio)
            .orElseThrow(() -> new RuntimeException("Precio de producto no encontrado"));

    // De existir, validamos que la cantidad del detalle cumpla con el requisito mínimo del precio
    if (existingPrecio.getCantidadRequerida() > 0){
      if (existing.getCantidad() <= existingPrecio.getCantidadRequerida()) {
        throw new RuntimeException("Para este precio, se necesita un minimo de " + existingPrecio.getCantidadRequerida() + " unidades del producto");
      }
    }
    
    existing.setSubtotal(existingPrecio.getPrecio().multiply(new BigDecimal(existing.getCantidad())));

    ordenDetalleRepository.save(existing);

    // Actualizar totales de la orden después de insertar el detalle
    ordenRepository.updateOrdenTotales(idOrden);

    return existing;
  }

  // Eliminar un detalle
  @DeleteMapping("/{idOrden}/{idOrdenDetalle}/{idProducto}")
  public void delete(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle) {

    OrdenDetallePK pk = new OrdenDetallePK(idOrden, idOrdenDetalle);
    ordenDetalleRepository.deleteById(pk);

    // Actualizar totales de la orden después de eliminar detalle
    ordenRepository.updateOrdenTotales(idOrden);
  }
}
