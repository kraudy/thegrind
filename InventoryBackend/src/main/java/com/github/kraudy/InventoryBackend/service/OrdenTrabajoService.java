package com.github.kraudy.InventoryBackend.service;

import com.github.kraudy.InventoryBackend.model.*;
import com.github.kraudy.InventoryBackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdenTrabajoService {

    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final OrdenSeguimientoRepository ordenSeguimientoRepository;
    private final ProductoTipoEstadoRepository productoTipoEstadoRepository;
    private final OrdenDetalleRepository ordenDetalleRepository;
    private final OrdenCalendarioRepository ordenCalendarioRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Registra progreso de trabajo (reparadores, normal, pegado, enmarcado, etc.)
     */
    @Transactional
    public OrdenTrabajo progresoTrabajo(Long idOrden, Long idOrdenDetalle, int cantidadTrabajada) {

        OrdenSeguimiento ordenSeguimientoActual = ordenSeguimientoRepository
                .findById(new OrdenSeguimientoPK(idOrden, idOrdenDetalle))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El detalle no se encuentra en seguimiento"));

        // Siguiente estado (para validación)
        ProductoTipoEstadoPK siguientePK = new ProductoTipoEstadoPK(
                ordenSeguimientoActual.getTipo(),
                ordenSeguimientoActual.getSubTipo(),
                ordenSeguimientoActual.getSecuencia() + 1);

        ProductoTipoEstado siguiente = productoTipoEstadoRepository.findById(siguientePK)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Siguiente estado no encontrado"));

        // Solo permitimos progreso en estos estados
        if (!List.of("Pegado", "Enmarcado", "Armado", "Calado", "Sublimacion", "Bodega", "Listo", "Entregado")
                .contains(siguiente.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El siguiente estado del producto no es válido para progreso: " + siguiente.getEstado());
        }

        // Determinar el estado del trabajo actual
        String estadoTrabajo = determinarEstadoTrabajo(ordenSeguimientoActual);

        OrdenTrabajoPK trabajoPK = new OrdenTrabajoPK(idOrden, idOrdenDetalle, estadoTrabajo);

        OrdenTrabajo trabajo = ordenTrabajoRepository.findById(trabajoPK)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No hay trabajo asignado para este estado en progreso trabajo: " + trabajoPK));

        if (cantidadTrabajada <= 0 ||
                cantidadTrabajada + trabajo.getCantidadTrabajada() > trabajo.getCantidadAsignada()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cantidad trabajada inválida: " + cantidadTrabajada);
        }

        trabajo.setCantidadTrabajada(trabajo.getCantidadTrabajada() + cantidadTrabajada);
        trabajo.setCantidadNoTrabajada(trabajo.getCantidadAsignada() - trabajo.getCantidadTrabajada());

        // TODO: agregar comentario vía DTO cuando el FE lo envíe
        return ordenTrabajoRepository.save(trabajo);
    }

    /**
     * Asigna trabajo inicial a un estado (reparación, normal, pegado, entrega, etc.) y la persona correspondiente
     */
    @Transactional
    public OrdenTrabajo asignarTrabajo(Long idOrden, Long idOrdenDetalle, String trabajador) {

        OrdenCalendario ordenCalendario = ordenCalendarioRepository.findById(idOrden)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La orden no se encuentra agendada"));

        OrdenDetalle detalle = ordenDetalleRepository.findById(new OrdenDetallePK(idOrden, idOrdenDetalle))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Detalle no encontrado"));

        OrdenSeguimiento seguimientoActual = ordenSeguimientoRepository
                .findById(new OrdenSeguimientoPK(idOrden, idOrdenDetalle))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El detalle no se encuentra en seguimiento"));

        // Siguiente estado
        ProductoTipoEstadoPK siguientePK = new ProductoTipoEstadoPK(
                seguimientoActual.getTipo(),
                seguimientoActual.getSubTipo(),
                seguimientoActual.getSecuencia() + 1);

        ProductoTipoEstado siguienteEstado = productoTipoEstadoRepository.findById(siguientePK)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Siguiente estado no encontrado"));

        if (!List.of("Reparacion", "Normal", "Enmarcado", "Pegado", "Armado", "Calado", "Sublimacion", "Entregado")
                .contains(siguienteEstado.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estado no válido para asignar trabajo: " + siguienteEstado.getEstado());
        }

        OrdenTrabajo trabajo = new OrdenTrabajo();
        trabajo.setIdOrden(idOrden);
        trabajo.setIdOrdenDetalle(idOrdenDetalle);
        trabajo.setEstado(siguienteEstado.getEstado());
        trabajo.setSecuencia(siguienteEstado.getSecuencia());
        trabajo.setTrabajador(trabajador);
        trabajo.setIdProducto(detalle.getIdProducto());
        trabajo.setFechaTrabajo(ordenCalendario.getFecha());
        trabajo.setComentario("");

        // === Rol y validaciones específicas por estado ===
        if ("Reparacion".equals(siguienteEstado.getEstado())) {
            if (!usuarioRepository.usuarioEsReparador(trabajador))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no es reparador");
            trabajo.setRol("repara");
            setCantidadInicial(trabajo, detalle.getCantidad());

        } else if ("Normal".equals(siguienteEstado.getEstado())) {
            if (!usuarioRepository.usuarioEsNormal(trabajador))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no es normal");
            trabajo.setRol("normal");
            setCantidadInicial(trabajo, detalle.getCantidad());

        } else if ("Entregado".equals(siguienteEstado.getEstado())) {
            if (!usuarioRepository.usuarioEntrega(trabajador))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no es entregador");
            trabajo.setRol("entrega");
            setCantidadDesdeEstadoAnterior(trabajo, seguimientoActual, true);

        } else if (List.of("Pegado", "Enmarcado", "Armado", "Calado", "Sublimacion")
                .contains(siguienteEstado.getEstado())) {

            if (seguimientoActual.getTipo().equals("Retablos")) {
                if (!usuarioRepository.usuarioEsPegador(trabajador))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no es pegador");
                trabajo.setRol("pega");
            } else {
                trabajo.setRol("alista");
            }

            setCantidadDesdeEstadoAnterior(trabajo, seguimientoActual, false);
        }

        return ordenTrabajoRepository.save(trabajo);
    }

    // ============================ Helpers ============================

    private String determinarEstadoTrabajo(OrdenSeguimiento seg) {
        String estado = seg.getEstado();

        if (List.of("Pegado", "Enmarcado", "Armado", "Calado", "Sublimacion", "Bodega").contains(estado)) {
            return estado;
        }
        if ("Listo".equals(estado)) {
            return "Entregado";
        }

        // Para los demás → estado anterior
        ProductoTipoEstadoPK prevPK = new ProductoTipoEstadoPK(
                seg.getTipo(), seg.getSubTipo(), seg.getSecuencia() - 1);
        return productoTipoEstadoRepository.findById(prevPK)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado anterior no encontrado"))
                .getEstado();
    }

    private void setCantidadInicial(OrdenTrabajo trabajo, int cantidad) {
        trabajo.setCantidadAsignada(cantidad);
        trabajo.setCantidadTrabajada(0);
        trabajo.setCantidadNoTrabajada(cantidad);
    }

    private void setCantidadDesdeEstadoAnterior(OrdenTrabajo trabajo, OrdenSeguimiento seg, boolean esEntrega) {
        int seqAnterior = esEntrega
                ? ((seg.getTipo().equals("Ampliaciones") || seg.getTipo().equals("Carita"))
                    ? seg.getSecuencia() - 2
                    : seg.getSecuencia() - 1)
                : seg.getSecuencia() - 1;   // Pegado/Enmarcado/etc. siempre toman del estado inmediatamente anterior

        ProductoTipoEstadoPK prevPK = new ProductoTipoEstadoPK(seg.getTipo(), seg.getSubTipo(), seqAnterior);
        ProductoTipoEstado prevEstado = productoTipoEstadoRepository.findById(prevPK)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Estado anterior no encontrado"));

        OrdenTrabajoPK pkAnterior = new OrdenTrabajoPK(
                seg.getIdOrden(), seg.getIdOrdenDetalle(), prevEstado.getEstado());

        OrdenTrabajo trabajoAnterior = ordenTrabajoRepository.findById(pkAnterior)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No hay trabajo asignado en el estado anterior"));

        trabajo.setCantidadAsignada(trabajoAnterior.getCantidadTrabajada());
        trabajo.setCantidadTrabajada(0);
        trabajo.setCantidadNoTrabajada(trabajoAnterior.getCantidadTrabajada());
    }

}