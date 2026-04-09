package com.github.kraudy.InventoryBackend.service;

import com.github.kraudy.InventoryBackend.dto.OrdenDetalleDTO;
import com.github.kraudy.InventoryBackend.dto.TrabajoEntregadoDTO;
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
public class FacturaDetalleService {
  private final OrdenCalendarioRepository ordenCalendarioRepository;

  private final FacturaDetalleRepository facturaDetalleRepository;
  private final FacturaRepository facturaRepository;

  private final OrdenTrabajoRepository ordenTrabajoRepository;
  private final OrdenRepository ordenRepository;
  private final OrdenDetalleRepository ordenDetalleRepository;
  private final ProductoRepository productoRepository;
  private final ProductoTipoEstadoRepository productoTipoEstadoRepository;
  private final OrdenSeguimientoRepository ordenSeguimientoRepository;

  private final NotificationService notificationService;

  @Transactional
  public Factura crearDesdeOrden(Long idOrden) {
    if (idOrden == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del orden es obligatorio");
    }

    // Cargamos la entidad Orden real
    Orden orden = ordenRepository.findById(idOrden)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Orden con id " + idOrden + " no encontrada"));

    //factura.setSoloFecha(factura.getFechaTrabajo().toLocalDate());

    String estadoFactura = facturaDetalleRepository.validarTipoPago(orden.getId());
    if (estadoFactura.equals("Pendiente")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede crear la factura porque no hay pagos aprobados");
    }

    String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

    Factura factura = new Factura();
    factura.setIdCliente(orden.getIdCliente());
    factura.setIdOrden(orden.getId());
    factura.setTotal(facturaDetalleRepository.obtenerTotalAFacturar(orden.getId()));
    factura.setUsuarioCreacion(currentUser);
    factura.setEstado(estadoFactura);
    // fechaCreacion se genera automaticamente

    for (TrabajoEntregadoDTO trabajoEntregado : ordenTrabajoRepository.getTrabajoEntregado(orden.getId())) {


      FacturaDetalle facturaDetalle = new FacturaDetalle();

      facturaDetalle.setIdOrdenDetalle(trabajoEntregado.idOrdenDetalle());
      facturaDetalle.setIdProducto(trabajoEntregado.idProducto());
      facturaDetalle.setPrecio(trabajoEntregado.precio());

      facturaDetalle.setCantidad(trabajoEntregado.cantidadEntregada());
      facturaDetalle.setSubtotal(trabajoEntregado.subtotal());
      facturaDetalle.setPrecio(trabajoEntregado.precio());
      facturaDetalle.setUsuarioCreacion(currentUser);

      // idDetalle se genera automáticamente en el SQL como el maximo + 1 para la factura dada, así que no lo seteamos aquí
      facturaDetalleRepository.insertDetalle(facturaDetalle);
      // fechaCreacion se obtiene automaticamente

    }

    // Actualiza vista de facturaciones en frontend
    notificationService.notifyFacturasChanged();

    return facturaRepository.save(factura);

  }
}