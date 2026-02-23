package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDTO;

import com.github.kraudy.InventoryBackend.model.OrdenSeguimiento;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoPK;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdenSeguimientoRepository extends JpaRepository<OrdenSeguimiento, OrdenSeguimientoPK> {
  @Query("SELECT s FROM OrdenSeguimiento s WHERE s.idOrden = :idOrden AND s.idOrdenDetalle = :idOrdenDetalle AND s.idProducto = :idProducto ORDER BY s.fechaCreacion ASC")
  List<OrdenSeguimiento> findByDetalleOrderByFechaCreacionAsc(Long idOrden, Long idOrdenDetalle, Long idProducto);

  @Query("SELECT s FROM OrdenSeguimiento s WHERE s.idOrden = :idOrden AND s.idOrdenDetalle = :idOrdenDetalle AND s.idProducto = :idProducto ORDER BY s.fechaCreacion DESC")
  List<OrdenSeguimiento> findByDetalleOrderByFechaCreacionDesc(Long idOrden, Long idOrdenDetalle, Long idProducto);

  List<OrdenSeguimiento> findByIdOrden(Long idOrden);

  @Query(value = """
    SELECT 
        s.id_orden,
        s.id_orden_detalle,
        s.id_producto,
        p.nombre,
        d.cantidad,
        p.tipo_producto,
        p.sub_tipo_producto,
        (SELECT estado 
         FROM orden_seguimiento ss 
         WHERE ss.id_orden = s.id_orden 
           AND ss.id_orden_detalle = s.id_orden_detalle 
           AND ss.id_producto = s.id_producto 
         ORDER BY ss.fecha_creacion DESC 
         LIMIT 1) AS estadoActual
    FROM orden_seguimiento s
    JOIN orden_detalle d ON d.id_orden = s.id_orden 
                        AND d.id_orden_detalle = s.id_orden_detalle 
                        AND d.id_producto = s.id_producto
    JOIN producto p ON p.id = s.id_producto
    WHERE s.id_orden = :idOrden
    GROUP BY s.id_orden, s.id_orden_detalle, s.id_producto, p.nombre, 
             d.cantidad,
             p.tipo_producto, p.sub_tipo_producto
    ORDER BY s.id_orden_detalle ASC
    """, nativeQuery = true)
  List<OrdenSeguimientoDTO> getFullSeguimientoByOrden(@Param("idOrden") Long idOrden);
}
