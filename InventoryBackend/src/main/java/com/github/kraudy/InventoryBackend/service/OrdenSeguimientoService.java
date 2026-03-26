package com.github.kraudy.InventoryBackend.service;

import com.github.kraudy.InventoryBackend.model.*;
import com.github.kraudy.InventoryBackend.repository.*;
import com.github.kraudy.InventoryBackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Transactional
    public OrdenSeguimiento reverseState(Long idOrden, Long idOrdenDetalle) {

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

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

        // === Business rules for reversing ===
        if (actual.getEstado().equals(EstadoSeguimientoEnum.LISTO.toString())) {
            ordenRepository.updateEstado(actual.getIdOrden(), "Repartida");
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

        actual = ordenSeguimientoRepository.save(actual);

        // Finish historical record
        finishHistorico(historico, actual);

        notificationService.notifyOrdenesSeguimientoChanged();

        return actual;
    }

    @Transactional
    public OrdenSeguimiento advanceState(Long idOrden, Long idOrdenDetalle) {

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        OrdenSeguimientoPK pk = new OrdenSeguimientoPK(idOrden, idOrdenDetalle);
        OrdenSeguimiento actual = ordenSeguimientoRepository.findById(pk)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El detalle no se encuentra en seguimiento"));

        // Estado siguiente 
        ProductoTipoEstadoPK nextPk = new ProductoTipoEstadoPK(
                actual.getTipo(), actual.getSubTipo(), actual.getSecuencia() + 1);
        ProductoTipoEstado siguiente = productoTipoEstadoRepository.findById(nextPk)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Siguiente estado no encontrado"));

        // === Create historical record ===
        OrdenSeguimientoHistorico historico = createHistorico(actual, currentUser);

        // Update current state
        actual.setSeguimientoPor(currentUser);
        actual.setEstado(siguiente.getEstado());        // uses enum.toString()
        actual.setSecuencia(siguiente.getSecuencia());

        actual = ordenSeguimientoRepository.save(actual);

        // Finish historical record
        finishHistorico(historico, actual);

        // === Business validations after advance ===
        validateAdvanceRules(actual, siguiente);

        notificationService.notifyOrdenesSeguimientoChanged();

        return actual;
    }

    // ==================== Private helper methods ====================

    private void deleteTrabajoIfNeeded(OrdenSeguimiento actual) {
        List<EstadoSeguimientoEnum> statesToDelete = List.of(
                EstadoSeguimientoEnum.REPARACION,
                EstadoSeguimientoEnum.NORMAL,
                EstadoSeguimientoEnum.IMPRESION,
                EstadoSeguimientoEnum.PEGADO,
                EstadoSeguimientoEnum.ENMARCADO
        );

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

    private void resetPreviousTrabajoIfNeeded(OrdenSeguimiento actual) {
        if (List.of(EstadoSeguimientoEnum.IMPRESION, EstadoSeguimientoEnum.ENMARCADO,
                EstadoSeguimientoEnum.PEGADO, EstadoSeguimientoEnum.LISTO,
                EstadoSeguimientoEnum.ENTREGADO).contains(EstadoSeguimientoEnum.fromString(actual.getEstado()))) {

            int seq = switch (EstadoSeguimientoEnum.fromString(actual.getEstado())) {
                case IMPRESION, LISTO -> actual.getSecuencia() - 1;
                case ENTREGADO -> actual.getSecuencia();
                default -> actual.getSecuencia() - 2;
            };

            ProductoTipoEstadoPK resetPk = new ProductoTipoEstadoPK(
                    actual.getTipo(), actual.getSubTipo(), seq);

            ProductoTipoEstado resetEstado = productoTipoEstadoRepository.findById(resetPk)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado anterior no encontrado"));

            OrdenTrabajoPK trabajoPk = new OrdenTrabajoPK(
                    actual.getIdOrden(), actual.getIdOrdenDetalle(), resetEstado.getEstado());

            OrdenTrabajo trabajo = ordenTrabajoRepository.findById(trabajoPk)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay trabajo asignado para este estado"));

            trabajo.setCantidadTrabajada(0);
            trabajo.setCantidadNoTrabajada(trabajo.getCantidadAsignada());
            ordenTrabajoRepository.save(trabajo);
        }
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

    private void finishHistorico(OrdenSeguimientoHistorico historico, OrdenSeguimiento actual) {
        historico.setFechaFinalizacion(actual.getFechaModificacion());
        historico.setUsuarioFinalizacion(actual.getSeguimientoPor());
        historico.setDuracion(Duration.between(
                historico.getFechaCreacion(), historico.getFechaFinalizacion()).toMinutes());
        ordenSeguimientoHistoricoRepository.save(historico);
    }

    private void validateAdvanceRules(OrdenSeguimiento actual, ProductoTipoEstado siguiente) {
        if (siguiente.getEstado().equals(EstadoSeguimientoEnum.REPARACION.toString())) {
            if (!ordenTrabajoRepository.detalleEstaAsignado(
                    actual.getIdOrden(), actual.getIdOrdenDetalle(), siguiente.getEstado())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No se puede avanzar a reparacion sin asignar detalle a reparador");
            }
        }

        if (siguiente.getEstado().equals(EstadoSeguimientoEnum.PEGADO.toString())) {
            if (!ordenTrabajoRepository.detalleEstaAsignado(
                    actual.getIdOrden(), actual.getIdOrdenDetalle(), siguiente.getEstado())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No se puede avanzar a pegado sin asignar detalle a pegador");
            }
        }

        if (siguiente.getEstado().equals(EstadoSeguimientoEnum.LISTO.toString())) {
            if (ordenSeguimientoRepository.estanTodosLosDetallesListos(actual.getIdOrden())) {
                ordenRepository.updateEstado(actual.getIdOrden(), siguiente.getEstado());
            }
        }

        if (siguiente.getEstado().equals(EstadoSeguimientoEnum.ENTREGADO.toString())) {
            if (ordenSeguimientoRepository.estanTodosLosDetallesEntregados(actual.getIdOrden())) {
                ordenRepository.updateEstado(actual.getIdOrden(), siguiente.getEstado());
            }
        }
    }
}