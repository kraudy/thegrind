package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.Orden;
import com.github.kraudy.InventoryBackend.model.OrdenDTO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
  @Query("""
    SELECT new com.github.kraudy.InventoryBackend.model.OrdenDTO(
        ord.id,
        ord.cliente.id,                                       
        CONCAT(cte.nombre, ' ', cte.apellido),                
        ord.creadaPor,
        ord.totalMonto,
        ord.totalProductos,
        ord.fechaCreacion,
        ord.fechaEntrega,
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
    SELECT new com.github.kraudy.InventoryBackend.model.OrdenDTO(
        ord.id,
        ord.cliente.id,                                       
        CONCAT(cte.nombre, ' ', cte.apellido),                
        ord.creadaPor,
        ord.totalMonto,
        ord.totalProductos,
        ord.fechaCreacion,
        ord.fechaEntrega,
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
}
