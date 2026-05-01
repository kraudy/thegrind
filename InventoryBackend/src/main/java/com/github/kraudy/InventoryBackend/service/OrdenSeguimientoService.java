package com.github.kraudy.InventoryBackend.service;

import com.github.kraudy.InventoryBackend.model.*;
import com.github.kraudy.InventoryBackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdenSeguimientoService {

    private final OrdenSeguimientoRepository ordenSeguimientoRepository;
    private final OrdenRepository ordenRepository;
    private final ProductoTipoEstadoRepository productoTipoEstadoRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final OrdenSeguimientoHistoricoRepository ordenSeguimientoHistoricoRepository;
    private final NotificationService notificationService;
    private final OrdenCostoService ordenCostoService;
    private final OrdenCostoRepository ordenCostoRepository;

    private final ProductoRepository productoRepository;
    private final OrdenDetalleRepository ordenDetalleRepository;

    private final CurrentUserService currentUserService;

    @Transactional
    public OrdenSeguimiento reverseState(Long idOrden, Long idOrdenDetalle) {

        String currentUser = currentUserService.getCurrentUser();

        OrdenSeguimientoPK pk = new OrdenSeguimientoPK(idOrden, idOrdenDetalle);
        OrdenSeguimiento actual = ordenSeguimientoRepository.findById(pk)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El detalle no se encuentra en seguimiento"));

        if (actual.getSecuencia() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay estado anterior disponible");
        }

        // Estado previo
        ProductoTipoEstadoPK prevPk = new ProductoTipoEstadoPK(
                actual.getTipo(), actual.getSubTipo(), actual.getSecuencia() - 1);
        ProductoTipoEstado previo = productoTipoEstadoRepository.findById(prevPk)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado anterior no encontrado"));

        // Si el estado era listo, se marca orden como repartida otra vez
        if (EstadoSeguimientoEnum.fromString(actual.getEstado()).equals(EstadoSeguimientoEnum.LISTO)) {
          ordenRepository.updateEstadoOrdenYFecha(actual.getIdOrden(), "Repartida");
        }

        // Delete associated OrdenTrabajo when going back
        deleteTrabajoIfNeeded(actual);

        // Reset previous trabajo when going back from certain states
        resetPreviousTrabajoIfNeeded(actual);

        // === Create historical record ===
        OrdenSeguimientoHistorico historico = createHistorico(actual, currentUser);

        // Update current state
        actual.setSeguimientoPor(currentUser);
        actual.setEstado(previo.getEstado());           // uses enum.toString()
        actual.setSecuencia(previo.getSecuencia());

        OrdenSeguimiento reversado = ordenSeguimientoRepository.save(actual);

        // Valida si eliminar o reversar los costos asociados al estado actual
        resetCostoTrabajoReparacionIfNeeded(reversado);
        deleteCostoIfNeeded(reversado);

        // Finish historical record
        finishHistorico(historico, reversado);

        notificationService.notifyOrdenesSeguimientoChanged();

        return reversado;
    }

    //TODO: Partir este servicio en dos. Crear dir de OrdenesSeguimientos y poner servicio para vanzar y otro para reversar
    @Transactional
    public OrdenSeguimiento advanceState(Long idOrden, Long idOrdenDetalle) {

        String currentUser = currentUserService.getCurrentUser();

        OrdenSeguimientoPK pk = new OrdenSeguimientoPK(idOrden, idOrdenDetalle);
        OrdenSeguimiento actual = ordenSeguimientoRepository.findById(pk)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El detalle no se encuentra en seguimiento"));

        // Estado siguiente 
        ProductoTipoEstadoPK nextPk = new ProductoTipoEstadoPK(
                actual.getTipo(), actual.getSubTipo(), actual.getSecuencia() + 1);
        ProductoTipoEstado siguiente = productoTipoEstadoRepository.findById(nextPk)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Siguiente estado no encontrado"));

        // Estado anterior 
        ProductoTipoEstado previo = null;
        EstadoSeguimientoEnum estadoPrevio = null;
        if (actual.getSecuencia() > 1) {
          ProductoTipoEstadoPK prevPk = new ProductoTipoEstadoPK(
                actual.getTipo(), actual.getSubTipo(), actual.getSecuencia() - 1);
          previo = productoTipoEstadoRepository.findById(prevPk)
                  .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado anterior no encontrado"));
        }
        

        EstadoSeguimientoEnum estadoActual = EstadoSeguimientoEnum.fromString(actual.getEstado());
        if (previo != null) {
          estadoPrevio = EstadoSeguimientoEnum.fromString(previo.getEstado());
        }

        // === Create historical record ===
        OrdenSeguimientoHistorico historico = createHistorico(actual, currentUser);
        
        // DEBE HACERSE ANTES DE ACTUALIZAR EL ESTADO o cambiarlo a validar el estado anterior
        /* Asigna costo de orden a ser pagado o cobrado */
        if (List.of(EstadoSeguimientoEnum.REPARACION).contains(estadoActual)) {
          ordenCostoService.asignarOrdenCosto(idOrden, idOrdenDetalle, estadoActual.toString());
        }

        if (List.of(EstadoSeguimientoEnum.IMPRESION).contains(estadoActual)) {
          // Se valida sub tipo porque las normales tambien comparte el estado impresion
          if (actual.getSubTipo().equals("Reparacion") ) {
            ordenCostoService.asignarOrdenCostoCantidadTrabajada(idOrden, idOrdenDetalle, estadoPrevio.toString());
          }
        }
        if (List.of(EstadoSeguimientoEnum.LISTO).contains(estadoActual)) {
          // Se valida sub tipo porque las normales tambien comparte el estado impresion
          if (actual.getTipo().equals("Retablos") ) {
            ordenCostoService.asignarOrdenCostoCantidadTrabajada(idOrden, idOrdenDetalle, estadoPrevio.toString());
          }
        }

        // Update current state
        actual.setSeguimientoPor(currentUser);
        actual.setEstado(siguiente.getEstado());        // uses enum.toString()
        actual.setSecuencia(siguiente.getSecuencia());

        OrdenSeguimiento avanzado = ordenSeguimientoRepository.save
        (actual);

        // Finish historical record
        finishHistorico(historico, avanzado);

        // === Business validations after advance ===
        validateAdvanceRules(avanzado, siguiente);
        validarActualizarEstadoOrden(avanzado, siguiente);

        notificationService.notifyOrdenesSeguimientoChanged();

        //TODO: VAlidar este, creo que tambien el afecta el cambio de estado pero al negarlo muestra el mensaje 
        /* Actualiza informacion relevante para el calendario */
        if (!List.of(EstadoSeguimientoEnum.REPARTIDA, EstadoSeguimientoEnum.REPARACION, 
          EstadoSeguimientoEnum.NORMAL, EstadoSeguimientoEnum.IMPRESION
        ).contains(EstadoSeguimientoEnum.fromString(avanzado.getEstado()))) {
          notificationService.notifyCalendarioChanged();
        }

        // Crea registro de costo
        if (List.of(EstadoSeguimientoEnum.PEGADO).contains(EstadoSeguimientoEnum.fromString(avanzado.getEstado()))) {
          ordenCostoService.asignarOrdenCosto(idOrden, idOrdenDetalle, avanzado.getEstado());
        }

        return avanzado;
    }

    // ==================== Private helper methods ====================

    private void deleteTrabajoIfNeeded(OrdenSeguimiento actual) {
        List<EstadoSeguimientoEnum> statesToDelete = List.of(
                EstadoSeguimientoEnum.REPARACION,
                EstadoSeguimientoEnum.NORMAL,
                EstadoSeguimientoEnum.IMPRESION,
                EstadoSeguimientoEnum.PEGADO,
                EstadoSeguimientoEnum.ENMARCADO,

                EstadoSeguimientoEnum.BODEGA,
                EstadoSeguimientoEnum.ARMADO,
                EstadoSeguimientoEnum.SUBLIMACION,
                EstadoSeguimientoEnum.CALADO
        );
        
        // Si el estado era Reparacion, Normal, Impresion, Enmarcado, Pegado, se elimina orden de trabajo asociada
        if (statesToDelete.contains(EstadoSeguimientoEnum.fromString(actual.getEstado()))) {
            OrdenTrabajoPK trabajoPk = new OrdenTrabajoPK(
                    actual.getIdOrden(), actual.getIdOrdenDetalle(), actual.getEstado());
            ordenTrabajoRepository.deleteById(trabajoPk);
            return;
        }

        if (EstadoSeguimientoEnum.LISTO.equals(EstadoSeguimientoEnum.fromString(actual.getEstado()))) {
          ProductoTipoEstadoPK productoTipoEstadoSiguientePK = new ProductoTipoEstadoPK(actual.getTipo(), actual.getSubTipo(), actual.getSecuencia() + 1);
          ProductoTipoEstado productoTipoEstadoSiguiente = productoTipoEstadoRepository.findById(productoTipoEstadoSiguientePK).
            orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado siguiente a Listo no encontrado"));
          OrdenTrabajoPK ordenTrabajoPK = new OrdenTrabajoPK(actual.getIdOrden(), actual.getIdOrdenDetalle(), productoTipoEstadoSiguiente.getEstado());
          ordenTrabajoRepository.deleteById(ordenTrabajoPK);
        }
    }

    private void deleteCostoIfNeeded(OrdenSeguimiento reversado) {
      EstadoSeguimientoEnum estadoActual = EstadoSeguimientoEnum.fromString(reversado.getEstado());
      if (!List.of(EstadoSeguimientoEnum.IMPRESION, EstadoSeguimientoEnum.REPARACION).
            contains(estadoActual)) {
          // El trabajo no requiere resetearse
          return;
      }

      int seq = 0;
      switch (estadoActual) {
        case IMPRESION:
          if (reversado.getTipo().equals("Retablos")) {
            seq = reversado.getSecuencia() + 1;
          } else {
            return;
          }
          break;
      
        case REPARACION:
          if (reversado.getSubTipo().equals("Reparacion")) {
            seq = reversado.getSecuencia();
          } else {
            return;
          }
          break;

        default:
          return;
      }

      ProductoTipoEstadoPK deletePk = new ProductoTipoEstadoPK(
              reversado.getTipo(), reversado.getSubTipo(), seq);

      ProductoTipoEstado deleteEstado = productoTipoEstadoRepository.findById(deletePk)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado anterior no encontrado"));

      OrdenCostoPK costoPk = new OrdenCostoPK(
              reversado.getIdOrden(), reversado.getIdOrdenDetalle(), deleteEstado.getEstado());
      
      OrdenCosto ordenCosto = ordenCostoRepository.findById(costoPk)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el costo asociado al detalle: " + estadoActual));

      /* Si el detalle de la orden ya se pago, no se puede reversar */
      if (ordenCosto.isPagado()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede eliminar un costo ya pagado");
      }

      ordenCostoRepository.deleteById(costoPk);
    }
    

    private void resetCostoTrabajoReparacionIfNeeded(OrdenSeguimiento reversado) {
      EstadoSeguimientoEnum estadoActual = EstadoSeguimientoEnum.fromString(reversado.getEstado());
      if (!List.of(EstadoSeguimientoEnum.IMPRESION, EstadoSeguimientoEnum.PEGADO).
            contains(estadoActual)) {
          // El trabajo no requiere resetearse
          return;
      }

      int seq = 0;
      switch (estadoActual) {
        case IMPRESION:
          if (reversado.getSubTipo().equals("Reparacion")) {
            seq = reversado.getSecuencia() - 1;
          } else {
            return;
          }
          break;
      
        case PEGADO:
          if (reversado.getTipo().equals("Retablos")) {
            seq = reversado.getSecuencia();
          } else {
            return;
          }
          break;

        default:
          return;
      }

      ProductoTipoEstadoPK resetPk = new ProductoTipoEstadoPK(
              reversado.getTipo(), reversado.getSubTipo(), seq);

      ProductoTipoEstado resetEstado = productoTipoEstadoRepository.findById(resetPk)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado anterior no encontrado"));

      OrdenCostoPK costoPk = new OrdenCostoPK(
              reversado.getIdOrden(), reversado.getIdOrdenDetalle(), resetEstado.getEstado());

      OrdenCosto costo = ordenCostoRepository.findById(costoPk)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay costo asignado para este estado:" + resetEstado.getEstado()));

      /* Si el detalle de la orden ya se pago, no se puede reversar */
      if (costo.isPagado()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede regresar el trabajo de un costo ya pagado");
      }

      costo.setCantidadTrabajada(0);

      ordenCostoRepository.save(costo);
    } 

    private void resetPreviousTrabajoIfNeeded(OrdenSeguimiento actual) {
        if (!List.of(EstadoSeguimientoEnum.IMPRESION, 
                EstadoSeguimientoEnum.ENMARCADO, EstadoSeguimientoEnum.ARMADO, EstadoSeguimientoEnum.CALADO, EstadoSeguimientoEnum.SUBLIMACION, EstadoSeguimientoEnum.PEGADO, 
                EstadoSeguimientoEnum.LISTO,
                EstadoSeguimientoEnum.ENTREGADO).contains(EstadoSeguimientoEnum.fromString(actual.getEstado()))) {
            // El trabajo no requiere resetearse
            return;
        }
        int seq = 0;

        if (actual.getTipo().equals("Ampliaciones")) {
          seq = switch (EstadoSeguimientoEnum.fromString(actual.getEstado())) {
            case IMPRESION -> actual.getSecuencia() - 1;
            case LISTO -> actual.getSecuencia() - 2;
            case ENTREGADO -> actual.getSecuencia();
            default -> actual.getSecuencia() - 2;
          };
        } else {
          seq = switch (EstadoSeguimientoEnum.fromString(actual.getEstado())) {
            case IMPRESION, LISTO -> actual.getSecuencia() - 1;
            case ENTREGADO -> actual.getSecuencia();
            default -> actual.getSecuencia() - 2;
          };
        }

        ProductoTipoEstadoPK resetPk = new ProductoTipoEstadoPK(
                actual.getTipo(), actual.getSubTipo(), seq);

        ProductoTipoEstado resetEstado = productoTipoEstadoRepository.findById(resetPk)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado anterior no encontrado"));

        OrdenTrabajoPK trabajoPk = new OrdenTrabajoPK(
                actual.getIdOrden(), actual.getIdOrdenDetalle(), resetEstado.getEstado());

        OrdenTrabajo trabajo = ordenTrabajoRepository.findById(trabajoPk)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay trabajo asignado para este estado para resetear:" + resetEstado.getEstado()));

        trabajo.setCantidadTrabajada(0);
        trabajo.setCantidadNoTrabajada(trabajo.getCantidadAsignada());
        ordenTrabajoRepository.save(trabajo);
    }

    private OrdenSeguimientoHistorico createHistorico(OrdenSeguimiento actual, String currentUser) {
        OrdenSeguimientoHistorico h = new OrdenSeguimientoHistorico();
        h.setIdOrden(actual.getIdOrden());
        h.setIdOrdenDetalle(actual.getIdOrdenDetalle());
        h.setEstado(actual.getEstado().toString());   // enum -> DB value
        h.setFechaCreacion(actual.getFechaModificacion());
        h.setUsuarioCreacion(currentUser);
        return h;
    }
    
    /* Cierra el historico de seguimiento */
    private void finishHistorico(OrdenSeguimientoHistorico historico, OrdenSeguimiento avanzado) {
      historico.setFechaFinalizacion(avanzado.getFechaModificacion());
      historico.setUsuarioFinalizacion(avanzado.getSeguimientoPor());
      historico.setDuracion(Duration.between(
              historico.getFechaCreacion(), historico.getFechaFinalizacion()).toMinutes());
      ordenSeguimientoHistoricoRepository.save(historico);
    }

    private void validateAdvanceRules(OrdenSeguimiento avanzado, ProductoTipoEstado siguiente) {
      if (!List.of(EstadoSeguimientoEnum.REPARACION,
                  EstadoSeguimientoEnum.PEGADO).contains(EstadoSeguimientoEnum.fromString(siguiente.getEstado()))) {
        return;
      }
      if (!ordenTrabajoRepository.detalleEstaAsignado(avanzado.getIdOrden(), avanzado.getIdOrdenDetalle(), siguiente.getEstado())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede avanzar a " + siguiente.getEstado() + " sin asignar detalle a trabajador");
      }
    }

    private void validarActualizarEstadoOrden(OrdenSeguimiento avanzado, ProductoTipoEstado siguiente) {
      //TODO: Aqui, una vez la orden esta entregada, agregar nuevo registro para facturacion, puede generarse aqui y caer en lista como ordenes para facturar
      // o incluso un nuevo estado en orden trabajo. Luego, una vez se factura la orden, se genera la factura en su propia tabla y se le relaciona la orden correspondiente  
      //TODO: Mover esto a un trigger
      if (!List.of(EstadoSeguimientoEnum.LISTO,
                  EstadoSeguimientoEnum.ENTREGADO).contains(EstadoSeguimientoEnum.fromString(avanzado.getEstado()))) {
        return;
      }

      if (!ordenSeguimientoRepository.estanTodosLosDetallesListos(avanzado.getIdOrden())) return;

      ordenRepository.updateEstadoOrdenYFecha(avanzado.getIdOrden(), siguiente.getEstado());
    }
}