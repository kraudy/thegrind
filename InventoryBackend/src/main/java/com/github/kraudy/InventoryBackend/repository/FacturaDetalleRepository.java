package com.github.kraudy.InventoryBackend.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.kraudy.InventoryBackend.dto.FacturaDetalleDTO;
import com.github.kraudy.InventoryBackend.model.FacturaDetalle;
import com.github.kraudy.InventoryBackend.model.FacturaDetallePK;

public interface FacturaDetalleRepository extends JpaRepository<FacturaDetalle, FacturaDetallePK> {

  @Query(value = """
    SELECT
      fd.id_factura AS idFactura,
      fd.id_detalle AS idDetalle,
      fd.id_orden_detalle AS idOrdenDetalle,
      fd.id_producto AS idProducto,
      prod.nombre AS nombreProducto,
      fd.precio AS precio,
      fd.cantidad AS cantidad,
      fd.subtotal AS subtotal,
      fd.usuario_creacion AS usuarioCreacion,
      fd.fecha_creacion AS fechaCreacion

    FROM factura_detalle fd

    INNER JOIN producto prod 
      ON (prod.id = fd.id_producto)
      
    LEFT JOIN orden_detalle odet 
      ON (odet.id_orden_detalle = fd.id_orden_detalle AND 
          odet.id_orden = (SELECT id_orden FROM factura WHERE id = fd.id_factura))

    WHERE fd.id_factura = :idFactura
    ORDER BY fd.id_detalle ASC
  """, nativeQuery = true)
  List<FacturaDetalleDTO> getFacturaDetalle(Long idFactura);

  @Modifying
  @Transactional
  @Query(value = """
    INSERT INTO factura_detalle (
        id_factura, id_detalle, 
        id_orden_detalle, id_producto, precio, cantidad, subtotal, usuario_creacion, fecha_creacion 
    ) VALUES (
        :idFactura, (SELECT COALESCE(MAX(id_detalle),0) + 1 FROM factura_detalle WHERE id_factura = :idFactura),
        :idOrdenDetalle, :idProducto,
        :precio, :cantidad, :subtotal,
        :usuarioCreacion, CURRENT_TIMESTAMP
    )
  """, nativeQuery = true)
  void insertDetalle(
    @Param("idFactura") Long idFactura,
    @Param("idOrdenDetalle") Long idOrdenDetalle,
    @Param("idProducto") Long idProducto,
    @Param("precio") BigDecimal precio,
    @Param("cantidad") int cantidad,
    @Param("subtotal") BigDecimal subtotal,
    @Param("usuarioCreacion") String usuarioCreacion
  );


  @Query(value = """
    WITH factura_total AS (
        SELECT 
            det.id_orden,
            SUM(det.precio_unitario * COALESCE(trabajoEntregado.cantidad_trabajada, 0)) AS totalFactura
        
        FROM orden_detalle det 

        LEFT JOIN orden_trabajo trabajoEntregado 
            ON trabajoEntregado.id_orden = det.id_orden
           AND trabajoEntregado.id_orden_detalle = det.id_orden_detalle
           AND trabajoEntregado.estado IN ('Entregado')

        WHERE det.id_orden = :idOrden
        GROUP BY det.id_orden
    ),
    pagos_totals AS (
        SELECT 
            p.id_orden,
            SUM(p.monto) AS totalPagos
        FROM orden_pago p
        WHERE p.estado = 'Aprobado' AND  -- Solo pagos aprobados
              p.id_orden = :idOrden
        GROUP BY p.id_orden
    )
    SELECT 
      CASE (factTot.totalFactura - COALESCE(ptot.totalPagos, 0))
          WHEN 0 THEN 'Saldo'                                         -- Completamente Pagada
          WHEN factTot.totalFactura THEN 'Pendiente'                  -- No se ha pagado nada 
          ELSE 'Parcial'                                              -- Se ha pagado algo pero no todo
      END AS estadoPago

    FROM factura_total factTot
    LEFT JOIN pagos_totals ptot 
      ON ptot.id_orden = factTot.id_orden

    WHERE factTot.id_orden = :idOrden
  """, nativeQuery = true)
  String validarTipoPago(@Param("idOrden") Long idOrden);

  @Query(value = """
    SELECT 
      SUM(det.precio_unitario * COALESCE(trabajoEntregado.cantidad_trabajada, 0)) AS totalFactura

    FROM orden_seguimiento seg

    JOIN orden_detalle det 
        ON det.id_orden = seg.id_orden 
        AND det.id_orden_detalle = seg.id_orden_detalle

    LEFT JOIN orden_trabajo trabajoEntregado 
        ON trabajoEntregado.id_orden = seg.id_orden
        AND trabajoEntregado.id_orden_detalle = seg.id_orden_detalle
        AND trabajoEntregado.estado IN ('Entregado')

    WHERE seg.estado IN ('Entregado') and seg.id_orden = :idOrden
    GROUP BY seg.id_orden
  """, nativeQuery = true)
  BigDecimal obtenerTotalAFacturar(@Param("idOrden") Long idOrden);

}
