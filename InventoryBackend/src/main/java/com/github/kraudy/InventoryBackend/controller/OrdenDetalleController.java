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
import org.springframework.web.bind.annotation.*;

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
  @GetMapping("/{idOrden}/{idOrdenDetalle}/{idProducto}")
  public OrdenDetalle getById(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @PathVariable Long idProducto) {

    OrdenDetallePK pk = new OrdenDetallePK(idOrden, idOrdenDetalle, idProducto);
    return ordenDetalleRepository.findById(pk).orElse(null);
  }

  // Crear un nuevo detalle
  @PostMapping("/{idOrden}/{idProducto}")
  public void create(
      @PathVariable Long idOrden,
      @PathVariable Long idProducto,
      @RequestBody OrdenDetalle ordenDetalle) {

    if (idOrden == null) {
        throw new IllegalArgumentException("El ID de la orden es obligatorio");
    }
    if (idProducto == null) {
        throw new IllegalArgumentException("El ID del producto es obligatorio");
    }

    ordenDetalleRepository.insertDetalle(idOrden, idProducto, ordenDetalle.getCantidad(), ordenDetalle.getPrecioUnitario(), ordenDetalle.getSubtotal());

    // Actualizar totales de la orden después de crear detalle
    ordenRepository.updateOrdenTotales(idOrden, idProducto);

    return;
  }

  // Actualizar un detalle existente
 @PutMapping("/{idOrden}/{idOrdenDetalle}/{idProducto}")
  public OrdenDetalle update(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @PathVariable Long idProducto,
          @RequestBody OrdenDetalle ordenDetalleActualizado) {

    // Construimos la PK para buscar la entidad existente
    OrdenDetallePK pk = new OrdenDetallePK(idOrden, idOrdenDetalle, idProducto);

    OrdenDetalle existing = ordenDetalleRepository.findById(pk)
            .orElseThrow(() -> new RuntimeException("Detalle de orden no encontrado"));

    // PK de precio del producto para validar que exista el precio antes de actualizar el detalle
    ProductoPrecioPK pkPrecio = new ProductoPrecioPK(idProducto, ordenDetalleActualizado.getPrecioUnitario());

    ProductoPrecio existingPrecio = productoPrecioRepository.findById(pkPrecio)
            .orElseThrow(() -> new RuntimeException("Precio de producto no encontrado"));

    // De existir, validamos que la cantidad del detalle cumpla con el requisito mínimo del precio
    if (existingPrecio.getCantidadRequerida() > 0){
      if (ordenDetalleActualizado.getCantidad() <= existingPrecio.getCantidadRequerida()) {
        throw new RuntimeException("Para este precio, se necesita un minimo de " + existingPrecio.getCantidadRequerida() + " unidades del producto");
      }
    }
    
    existing.setCantidad(ordenDetalleActualizado.getCantidad());
    existing.setPrecioUnitario(existingPrecio.getPrecio());
    existing.setSubtotal(existingPrecio.getPrecio().multiply(new BigDecimal(ordenDetalleActualizado.getCantidad())));

    ordenDetalleRepository.save(existing);

    // Actualizar totales de la orden después de insertar el detalle
    ordenRepository.updateOrdenTotales(idOrden, idProducto);

    return existing;
  }

  // Eliminar un detalle
  @DeleteMapping("/{idOrden}/{idOrdenDetalle}/{idProducto}")
  public void delete(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @PathVariable Long idProducto) {

    OrdenDetallePK pk = new OrdenDetallePK(idOrden, idOrdenDetalle, idProducto);
    ordenDetalleRepository.deleteById(pk);

    // Actualizar totales de la orden después de eliminar detalle
    ordenRepository.updateOrdenTotales(idOrden, idProducto);
  }
}
