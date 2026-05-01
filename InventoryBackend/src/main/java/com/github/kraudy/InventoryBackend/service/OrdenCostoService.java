package com.github.kraudy.InventoryBackend.service;

import com.github.kraudy.InventoryBackend.model.OrdenDetallePK;
import com.github.kraudy.InventoryBackend.model.OrdenTrabajoPK;
import com.github.kraudy.InventoryBackend.model.ProductoCosto;
import com.github.kraudy.InventoryBackend.model.ProductoCostoPK;
import com.github.kraudy.InventoryBackend.model.OrdenTrabajo;
import com.github.kraudy.InventoryBackend.model.OrdenDetalle;
import com.github.kraudy.InventoryBackend.model.OrdenCosto;
import com.github.kraudy.InventoryBackend.model.OrdenCostoPK;
import com.github.kraudy.InventoryBackend.repository.*;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrdenCostoService {

    private final OrdenCostoRepository ordenCostoRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final OrdenDetalleRepository ordenDetalleRepository;
    private final ProductoCostoRepository productoCostoRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public OrdenCosto asignarOrdenCosto(Long idOrden, Long idOrdenDetalle, String estadoTrabajo) {

      OrdenTrabajoPK trabajoPK = new OrdenTrabajoPK(idOrden, idOrdenDetalle, estadoTrabajo);

      OrdenTrabajo trabajo = ordenTrabajoRepository.findById(trabajoPK)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                      "No hay trabajo asignado para este estado: " + trabajoPK));

      OrdenDetallePK ordenDetallePk = new OrdenDetallePK(idOrden, idOrdenDetalle);

      OrdenDetalle ordenDetalle = ordenDetalleRepository.findById(ordenDetallePk)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                      "No se encontró el detalle de orden: " + ordenDetallePk));

      ProductoCostoPK productoCostoPk  = new ProductoCostoPK(trabajo.getIdProducto(), estadoTrabajo);
      ProductoCosto productoCosto = productoCostoRepository.findById(productoCostoPk).orElse(null);

      OrdenCosto ordenCosto = new OrdenCosto();
      ordenCosto.setIdOrden(idOrden);
      ordenCosto.setIdOrdenDetalle(idOrdenDetalle);
      ordenCosto.setTipoCosto(estadoTrabajo);
      ordenCosto.setTrabajador(trabajo.getTrabajador());
      ordenCosto.setRol(trabajo.getRol());

      ordenCosto.setIdProducto(trabajo.getIdProducto());

      ordenCosto.setCantidadOrden(ordenDetalle.getCantidad());
      ordenCosto.setCantidadAsignada(trabajo.getCantidadAsignada());
      // Esta agregarla aparte, en otro servicio cuando el estado actual sea IMPRESION y se avance bien, y solo actualizar la cantidad trabajada.
      //ordenCosto.setCantidadTrabajada(trabajo.getCantidadTrabajada());
                        ordenCosto.setCosto(productoCosto != null ? productoCosto.getCosto() : BigDecimal.ZERO);

      ordenCosto.setComentario(trabajo.getComentario());
      ordenCosto.setFechaTrabajo(trabajo.getFechaTrabajo());

      ordenCosto.setUsuarioCreacion(currentUserService.getCurrentUser());

      return ordenCostoRepository.save(ordenCosto);
        
    }

    @Transactional
    public OrdenCosto asignarOrdenCostoCantidadTrabajada(Long idOrden, Long idOrdenDetalle, String estadoTrabajo) {

      OrdenTrabajoPK trabajoPK = new OrdenTrabajoPK(idOrden, idOrdenDetalle, estadoTrabajo);

      OrdenTrabajo trabajo = ordenTrabajoRepository.findById(trabajoPK)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                      "No hay trabajo asignado para este estado: " + trabajoPK));

      OrdenCostoPK ordenCostoPK = new OrdenCostoPK(idOrden, idOrdenDetalle, estadoTrabajo);
      OrdenCosto ordenCostoExistente = ordenCostoRepository.findById(ordenCostoPK)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                      "No se encontró el costo para esta orden, detalle y tipo de trabajo: " + ordenCostoPK));

      ordenCostoExistente.setCantidadTrabajada(trabajo.getCantidadTrabajada());

      return ordenCostoRepository.save(ordenCostoExistente);  
    }

    @Transactional
    public void pagarOrdenCosto(String tipoCosto, String trabajador, LocalDate fechaInicio, LocalDate fechaFin, Long idOrden, Long idOrdenDetalle) {
          if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
                  throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                  "La fechaInicio no puede ser mayor que fechaFin");
          }

          //TODO: Agregar detalle, fecha inicio y fin a la consutla del repositorio como opcionales para optimizar
          List<OrdenCosto> costosPendientes = ordenCostoRepository.obtenerOrdenes(tipoCosto, trabajador, fechaInicio, fechaFin, idOrden, idOrdenDetalle, false);

          if (costosPendientes.isEmpty()) {
                  throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                  "No hay costos pendientes para pagar con los filtros enviados");
          }

          String usuarioPaga = currentUserService.getCurrentUser();
          LocalDate fechaPago = LocalDate.now();

          costosPendientes.forEach(costo -> {
                  costo.setPagado(true);
                  costo.setUsuarioPaga(usuarioPaga);
                  costo.setFechaPago(fechaPago);
          });

          ordenCostoRepository.saveAll(costosPendientes);
    }

}