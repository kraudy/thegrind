package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.OrdenPagoDTO;
import com.github.kraudy.InventoryBackend.model.OrdenPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface OrdenPagoRepository extends JpaRepository<OrdenPago, Long> {

    // All payments for a specific order
    List<OrdenPago> findByIdOrden(Long idOrden);

    // All pending payments (for the approval screen)
    @Query(value = """
        SELECT 
            SUM(COALESCE(op.monto, 0))

        FROM orden_pago op
        WHERE op.id_orden = :idOrden
        """, nativeQuery = true)
    BigDecimal getTotalPagado(@Param("idOrden") Long idOrden);

    // All pending payments (for the approval screen)
    @Query(value = """
        SELECT 
            op.id,
            op.id_orden as idOrden,
            op.monto,
            op.fecha_pago as fechaPago,
            op.metodo_pago as metodoPago,
            op.estado,
            op.recibido_por as recibidoPor,
            op.aprobado_por as aprobadoPor,
            op.fecha_aprobacion as fechaAprobacion,
            op.notas

        FROM orden_pago op
        WHERE op.estado IN ('Pendiente')
        ORDER BY op.fecha_pago DESC
        """, nativeQuery = true)
    List<OrdenPagoDTO> findAllPendientes();

    // Approve a payment
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE orden_pago 
        SET estado = 'Aprobado', 
            aprobado_por = :aprobadoPor,
            fecha_aprobacion = CURRENT_TIMESTAMP
        WHERE id = :id
        """, nativeQuery = true)
    void aprobarPago(@Param("id") Long id, @Param("aprobadoPor") String aprobadoPor);

    // Reject a payment
    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM orden_pago
        WHERE id = :id and estado = 'Pendiente'
        """, nativeQuery = true)
    void rechazarPago(@Param("id") Long id, @Param("aprobadoPor") String aprobadoPor);

    @Query(value = """
      SELECT 
          op.id,
          op.id_orden as idOrden,
          CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
          ord.total_monto AS totalMonto,
          op.monto,
          op.fecha_pago as fechaPago,
          op.metodo_pago as metodoPago,
          op.codigo_referencia as codigoReferencia,
          op.banco,
          op.estado,
          op.recibido_por as recibidoPor,
          op.aprobado_por as aprobadoPor,
          op.fecha_aprobacion as fechaAprobacion,
          op.notas,
          op.tipo_pago as tipoPago

      FROM orden_pago op
      INNER JOIN orden ord ON ord.id = op.id_orden
      INNER JOIN cliente cte ON cte.id = ord.id_cliente
      WHERE (:search IS NULL OR 
            CAST(op.id_orden AS TEXT) ILIKE '%' || LOWER(:search) || '%' OR
            LOWER(CONCAT(cte.nombre, ' ', cte.apellido)) ILIKE '%' || LOWER(:search) || '%')
        AND (:estado IS NULL OR op.estado = :estado)
      ORDER BY op.fecha_pago DESC
    """, nativeQuery = true)
    List<OrdenPagoDTO> findAllWithFilter(@Param("search") String search, @Param("estado") String estado);
}