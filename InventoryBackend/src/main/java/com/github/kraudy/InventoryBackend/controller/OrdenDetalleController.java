package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.model.OrdenDetalle;
import com.github.kraudy.InventoryBackend.model.OrdenDetalleDTO;
import com.github.kraudy.InventoryBackend.model.OrdenDetallePK;
import com.github.kraudy.InventoryBackend.repository.OrdenDetalleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-detalle")
@CrossOrigin(origins = "http://localhost:4200")
public class OrdenDetalleController {
  @Autowired
  private OrdenDetalleRepository ordenDetalleRepository;

  // Obtener todos los detalles (de todas las órdenes)
  @GetMapping
  public List<OrdenDetalle> getAll() {
    return ordenDetalleRepository.findAll();
  }

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
  @PostMapping
  public void create(@RequestBody OrdenDetalle ordenDetalle) {
    if (ordenDetalle.getOrden() == null || ordenDetalle.getOrden().getId() == null) {
        throw new IllegalArgumentException("El ID de la orden es obligatorio");
    }
    if (ordenDetalle.getProducto() == null || ordenDetalle.getProducto().getId() == null) {
        throw new IllegalArgumentException("El ID del producto es obligatorio");
    }

    ordenDetalleRepository.insertDetalle(ordenDetalle.getOrden().getId(), ordenDetalle.getProducto().getId(), ordenDetalle.getCantidad(), ordenDetalle.getPrecioUnitario(), ordenDetalle.getSubtotal());

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

    // Solo actualizamos los campos mutables (cantidad, precio, subtotal)
    // Ignoramos cualquier intento de cambiar la PK (orden, producto o número de línea)
    existing.setCantidad(ordenDetalleActualizado.getCantidad());
    existing.setPrecioUnitario(ordenDetalleActualizado.getPrecioUnitario());
    existing.setSubtotal(ordenDetalleActualizado.getSubtotal());

    // Opcional: si quieres validar que el subtotal coincida con cálculo, puedes hacerlo aquí

    // Guardamos la entidad existente (Hibernate actualizará solo los campos permitidos)
    return ordenDetalleRepository.save(existing);
  }

  // Eliminar un detalle
  @DeleteMapping("/{idOrden}/{idOrdenDetalle}/{idProducto}")
  public void delete(
          @PathVariable Long idOrden,
          @PathVariable Long idOrdenDetalle,
          @PathVariable Long idProducto) {

    OrdenDetallePK pk = new OrdenDetallePK(idOrden, idOrdenDetalle, idProducto);
    ordenDetalleRepository.deleteById(pk);
  }
}
