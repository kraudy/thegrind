package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.OrdenDetalle;
import com.github.kraudy.InventoryBackend.model.OrdenDetallePK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenDetalleRepository extends JpaRepository<OrdenDetalle, OrdenDetallePK> {

  // Método útil para obtener todos los detalles de una orden específica
  List<OrdenDetalle> findByIdOrden(Long idOrden);
}
