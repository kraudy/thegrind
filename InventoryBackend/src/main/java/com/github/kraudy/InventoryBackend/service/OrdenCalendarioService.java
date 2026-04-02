package com.github.kraudy.InventoryBackend.service;

import com.github.kraudy.InventoryBackend.dto.OrdenDetalleDTO;
import com.github.kraudy.InventoryBackend.model.*;
import com.github.kraudy.InventoryBackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrdenCalendarioService {
  private final OrdenCalendarioRepository ordenCalendarioRepository;
  private final OrdenRepository ordenRepository;
  private final OrdenDetalleRepository ordenDetalleRepository;
  private final ProductoRepository productoRepository;
  private final ProductoTipoEstadoRepository productoTipoEstadoRepository;
  private final OrdenSeguimientoRepository ordenSeguimientoRepository;

  @Transactional
  public OrdenCalendario create(OrdenCalendario ordenCalendario) {

    if (ordenCalendario.getIdOrden() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idOrden es obligatorio");
    }

    // Cargamos la entidad Orden real
    Orden orden = ordenRepository.findById(ordenCalendario.getIdOrden())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Orden con id " + ordenCalendario.getIdOrden() + " no encontrada"));

    ordenCalendario.setFecha(ordenCalendario.getFechaTrabajo().toLocalDate());

    // Actualiza el estado de la orden
    orden.setEstado("Repartida");
    ordenRepository.save(orden);

    String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

    // Set audit fields
    ordenCalendario.setUsuarioCreacion(currentUser);
    ordenCalendario.setUsuarioModificacion(currentUser);

    // Genera el seguimiento inicial de cada detalle de la orden
    // (el TODO de mover esto a SQL sigue pendiente, pero ahora está encapsulado aquí)
    for (OrdenDetalleDTO ordenDetalle : ordenDetalleRepository.getAllOrdenDetalle(orden.getId())) {
      Producto producto = productoRepository.findById(ordenDetalle.idProducto())
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

      ProductoTipoEstadoPK productoTipoEstadoPK = new ProductoTipoEstadoPK(
              producto.getProductoTipo().getTipo(),
              producto.getSubTipoProducto(),
              1);

      ProductoTipoEstado productoTipoEstado = productoTipoEstadoRepository.findById(productoTipoEstadoPK)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado inicial de Producto no encontrado"));

      OrdenSeguimiento ordenSeguimiento = new OrdenSeguimiento();

      ordenSeguimiento.setIdOrden(ordenDetalle.idOrden());
      ordenSeguimiento.setIdOrdenDetalle(ordenDetalle.idOrdenDetalle());
      ordenSeguimiento.setTipo(producto.getTipoProducto());
      ordenSeguimiento.setSubTipo(producto.getSubTipoProducto());
      ordenSeguimiento.setEstado(productoTipoEstado.getEstado());
      ordenSeguimiento.setSecuencia(productoTipoEstado.getSecuencia());
      ordenSeguimiento.setSeguimientoPor(currentUser);

      ordenSeguimientoRepository.save(ordenSeguimiento);
    }

    return ordenCalendarioRepository.save(ordenCalendario);
  }
}