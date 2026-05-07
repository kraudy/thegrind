package com.github.kraudy.InventoryBackend.service;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import com.github.kraudy.InventoryBackend.model.OrdenEstadoEnum;
import com.github.kraudy.InventoryBackend.model.Orden;
import com.github.kraudy.InventoryBackend.model.OrdenPago;
import com.github.kraudy.InventoryBackend.repository.FacturaDetalleRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenCostoRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenDetalleRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenPagoRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenTrabajoRepository;
import com.github.kraudy.InventoryBackend.repository.ProductoCostoRepository;

@Service
@RequiredArgsConstructor
public class OrdenPagoService {

  private final OrdenCostoRepository ordenCostoRepository;
  private final OrdenTrabajoRepository ordenTrabajoRepository;
  private final OrdenDetalleRepository ordenDetalleRepository;
  private final ProductoCostoRepository productoCostoRepository;
  private final CurrentUserService currentUserService;

  private final OrdenPagoRepository ordenPagoRepository;
  private final OrdenRepository ordenRepository;
  private final NotificationService notificationService;
  private final FacturaDetalleRepository facturaDetalleRepository;
  
  @Transactional
  public OrdenPago registrarPago(Long idOrden, OrdenPago pago) {
    pago.setIdOrden(idOrden);
    // Estado is set to Pendiente by the database

    // Validar estado de la orden para determinar el total
    Orden orden = ordenRepository.findById(idOrden)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Orden con id " + idOrden + " no encontrada"));
    
    OrdenEstadoEnum estadoOrden = OrdenEstadoEnum.fromString(orden.getEstado());

    if (estadoOrden.equals(OrdenEstadoEnum.FACTURADO)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pago a orden facturada debe hacerse a través de la factura, no de la orden");
    }

    BigDecimal total = new BigDecimal(0.00);

    /* Determina el total segun el estado de la orden */
    if (estadoOrden.equals(OrdenEstadoEnum.ENTREGADO)) {
      total = facturaDetalleRepository.obtenerTotalAFacturar(idOrden);
    } else {
      total = orden.getTotalMonto();
    }
    
    BigDecimal totalPagado = ordenPagoRepository.getTotalPagado(idOrden);

    /* Si el total pagado sobre pasa el total a pagar, error */
    if (totalPagado.add(pago.getMonto()).compareTo(total) > 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto del pago excede el total pendiente de la orden");
    }

    /* Valida si es el pago final o adelanto */
    if (totalPagado.add(pago.getMonto()).compareTo(total) == 0) {
      pago.setTipoPago("Saldo");
    } else {
      pago.setTipoPago("Adelanto");
    }

    OrdenPago saved = ordenPagoRepository.save(pago);

    notificationService.notifyOrdenesPagoChanged();   // Notifica

    return saved;
  }

}
