package com.github.kraudy.InventoryBackend.service;

import com.github.kraudy.InventoryBackend.dto.TrabajoEntregadoDTO;
import com.github.kraudy.InventoryBackend.model.*;
import com.github.kraudy.InventoryBackend.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FacturaService {
  private final FacturaDetalleRepository facturaDetalleRepository;
  private final CurrentUserService currentUserService;
  private final FacturaRepository facturaRepository;

  private final OrdenTrabajoRepository ordenTrabajoRepository;
  private final OrdenRepository ordenRepository;

  private final NotificationService notificationService;

  @Transactional
  public Factura crearFactura(Long idOrden) {
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

    String currentUser = currentUserService.getCurrentUser();

    Factura factura = new Factura();
    factura.setIdCliente(orden.getIdCliente());
    factura.setIdOrden(orden.getId());
    factura.setTotal(facturaDetalleRepository.obtenerTotalAFacturar(orden.getId()));
    factura.setUsuarioCreacion(currentUser);
    factura.setEstado(estadoFactura);
    // fechaCreacion se genera automaticamente

    Factura facturaCreada = facturaRepository.save(factura);

    for (TrabajoEntregadoDTO trabajoEntregado : ordenTrabajoRepository.getTrabajoEntregado(orden.getId())) {
      // idDetalle se genera automáticamente en SQL para cada factura.
      facturaDetalleRepository.insertDetalle(
        facturaCreada.getId(),
        trabajoEntregado.idOrdenDetalle(),
        trabajoEntregado.idProducto(),
        trabajoEntregado.precio(),
        trabajoEntregado.cantidadEntregada(),
        trabajoEntregado.subtotal(),
        currentUser
      );

    }

    // Actualizamos estado de orden a "Facturado"
    orden.setEstado("Facturado");
    ordenRepository.save(orden);

    // Actualiza vista de facturaciones en frontend
    notificationService.notifyFacturasChanged();

    return facturaCreada;

  }
}