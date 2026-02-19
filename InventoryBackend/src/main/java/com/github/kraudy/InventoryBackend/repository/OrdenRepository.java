package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.OrdenDTO;
import com.github.kraudy.InventoryBackend.model.Orden;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
  @Query("""
    SELECT new com.github.kraudy.InventoryBackend.dto.OrdenDTO(
        ord.id,
        ord.cliente.id,                                       
        CONCAT(cte.nombre, ' ', cte.apellido),                
        ord.creadaPor,
        ord.totalMonto,
        ord.totalProductos,
        ord.fechaCreacion,
        ord.fechaVencimiento,
        ord.fechaPreparada,
        ord.fechaDespachada,
        ord.fechaModificacion,
        ord.estado
    )
    FROM Orden ord
    JOIN ord.cliente cte
    ORDER BY ord.id ASC
  """)
  List<OrdenDTO> getAll();

  @Query("""
    SELECT new com.github.kraudy.InventoryBackend.dto.OrdenDTO(
        ord.id,
        ord.cliente.id,                                       
        CONCAT(cte.nombre, ' ', cte.apellido),                
        ord.creadaPor,
        ord.totalMonto,
        ord.totalProductos,
        ord.fechaCreacion,
        ord.fechaVencimiento,
        ord.fechaPreparada,
        ord.fechaDespachada,
        ord.fechaModificacion,
        ord.estado
    )
    FROM Orden ord
    JOIN ord.cliente cte
    WHERE ord.id = :id
    ORDER BY ord.id ASC
  """)
  OrdenDTO getOrdenById(Long id);

  //TODO: Move this to a before insert,update, delete trigger in the database
  @Modifying
  @Transactional  // Required for DML operations
  @Query(value = """
    UPDATE orden
    SET 
        total_monto = (SELECT COALESCE(SUM(subtotal), 0) FROM orden_detalle WHERE id_orden = :idOrden), 
        total_productos = (SELECT COALESCE(SUM(cantidad), 0) FROM orden_detalle WHERE id_orden = :idOrden)
    WHERE id = :idOrden
  """, nativeQuery = true)
  void updateOrdenTotales(
      Long idOrden,
      Long idProducto
  );

  @Query("""
    SELECT new com.github.kraudy.InventoryBackend.dto.OrdenDTO(
        ord.id,
        ord.cliente.id,                                       
        CONCAT(cte.nombre, ' ', cte.apellido),                
        ord.creadaPor,
        ord.totalMonto,
        ord.totalProductos,
        ord.fechaCreacion,
        ord.fechaVencimiento,
        ord.fechaPreparada,
        ord.fechaDespachada,
        ord.fechaModificacion,
        ord.estado
    )
    FROM Orden ord JOIN ord.cliente cte
    WHERE ord.estado = 'Pendiente'
    ORDER BY ord.fechaVencimiento ASC
  """)
  List<OrdenDTO> getPendientes();
}
