package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDTO;

import com.github.kraudy.InventoryBackend.model.OrdenSeguimiento;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoPK;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdenSeguimientoRepository extends JpaRepository<OrdenSeguimiento, OrdenSeguimientoPK> {
  @Query("SELECT s FROM OrdenSeguimiento s WHERE s.idOrden = :idOrden AND s.idOrdenDetalle = :idOrdenDetalle ORDER BY s.fechaCreacion ASC")
  List<OrdenSeguimiento> findByDetalleOrderByFechaCreacionAsc(Long idOrden, Long idOrdenDetalle);

  @Query("SELECT s FROM OrdenSeguimiento s WHERE s.idOrden = :idOrden AND s.idOrdenDetalle = :idOrdenDetalle ORDER BY s.fechaCreacion DESC")
  List<OrdenSeguimiento> findByDetalleOrderByFechaCreacionDesc(Long idOrden, Long idOrdenDetalle);

  List<OrdenSeguimiento> findByIdOrden(Long idOrden);

  @Query(value = """
    SELECT
        seg.id_orden,
        seg.id_orden_detalle,
        det.id_producto,
        prod.nombre,
        det.cantidad,
        seg.tipo,
        seg.sub_tipo,
        seg.estado AS estadoActual
    FROM orden_seguimiento seg
    JOIN orden_detalle det ON det.id_orden = seg.id_orden
                        AND det.id_orden_detalle = seg.id_orden_detalle
    JOIN producto prod ON prod.id = det.id_producto
    WHERE seg.id_orden = :idOrden
    ORDER BY seg.id_orden_detalle ASC
    """, nativeQuery = true)
  List<OrdenSeguimientoDTO> getFullSeguimientoByOrden(@Param("idOrden") Long idOrden);

  //TODO: Agregar obtenerOrdenesPorEstado y pasarle el estado
}
