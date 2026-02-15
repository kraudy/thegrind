package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.OrdenDetalleDTO;
import com.github.kraudy.InventoryBackend.model.OrdenDetalle;
import com.github.kraudy.InventoryBackend.model.OrdenDetallePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface OrdenDetalleRepository extends JpaRepository<OrdenDetalle, OrdenDetallePK> {

  // Método útil para obtener todos los detalles de una orden específica
  List<OrdenDetalle> findByIdOrden(Long idOrden);

  @Query("""
    SELECT new com.github.kraudy.InventoryBackend.model.OrdenDetalleDTO(
        odet.idOrden,
        odet.idProducto,
        odet.producto.nombre,
        odet.idOrdenDetalle,
        odet.cantidad,
        odet.precioUnitario,
        odet.subtotal,
        odet.fechaCreacion,
        odet.fechaModificacion
    )
    FROM OrdenDetalle odet
    JOIN odet.producto prod
    WHERE odet.idOrden = :idOrden
    ORDER BY odet.idOrdenDetalle ASC
  """)
  List<OrdenDetalleDTO> getAllOrdenDetalle(Long idOrden);

  @Modifying
  @Transactional  // Required for DML operations
  @Query(value = """
    INSERT INTO orden_detalle (
        id_orden, id_producto, id_orden_detalle,
        cantidad, precio_unitario, subtotal,
        fecha_creacion, fecha_modificacion
    ) VALUES (
        :idOrden, :idProducto, (SELECT COALESCE(MAX(id_orden_detalle),0) + 1 FROM orden_detalle WHERE id_orden = :idOrden),
        :cantidad, :precioUnitario, :subtotal,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    )
  """, nativeQuery = true)
  void insertDetalle(
      Long idOrden,
      Long idProducto,
      int cantidad,
      BigDecimal precioUnitario,
      BigDecimal subtotal
  );
}
