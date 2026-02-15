package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.OrdenDetalleDTO;
import com.github.kraudy.InventoryBackend.model.OrdenDetalle;
import com.github.kraudy.InventoryBackend.model.OrdenDetallePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
