package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.OrdenCostoDTO;
import com.github.kraudy.InventoryBackend.model.OrdenCosto;
import com.github.kraudy.InventoryBackend.model.OrdenCostoPK;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface OrdenCostoRepository extends JpaRepository<OrdenCosto, OrdenCostoPK> {
  @Query("""
    SELECT new com.github.kraudy.InventoryBackend.dto.OrdenCostoDTO(
      oc.idOrden,
      oc.idOrdenDetalle,
      oc.tipoCosto,
      oc.trabajador,
      oc.rol,
      oc.idProducto,
      oc.cantidadOrden,
      oc.cantidadAsignada,
      oc.cantidadTrabajada,
      oc.costo,
      oc.pagado,
      oc.usuarioPaga,
      oc.fechaPago,
      oc.comentario,
      oc.fechaTrabajo,
      oc.fechaCreacion,
      oc.usuarioCreacion,
      oc.fechaModificacion
    )
    FROM OrdenCosto oc
    WHERE  oc.trabajador = :trabajador
      AND  oc.tipoCosto = :tipoCosto
      AND  oc.idOrden = COALESCE(:idOrden, oc.idOrden)
      AND  oc.pagado = COALESCE(:pagado, oc.pagado)
      AND  oc.fechaTrabajo >= COALESCE(:fechaInicio, oc.fechaTrabajo)
      AND  oc.fechaTrabajo <= COALESCE(:fechaFin, oc.fechaTrabajo)
      AND  oc.idOrdenDetalle = COALESCE(:idOrdenDetalle, oc.idOrdenDetalle)
    ORDER BY oc.trabajador, oc.fechaTrabajo DESC
    """)
  List<OrdenCostoDTO> obtenerOrdenesDTO(
      @Param("tipoCosto") String tipoCosto, @Param("trabajador") String trabajador,
      @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin,
      @Param("idOrden") Long idOrden, @Param("idOrdenDetalle") Long idOrdenDetalle,
      @Param("pagado") Boolean pagado);

  @Query(value = """
    SELECT oc.*

    FROM orden_costo oc

    WHERE  oc.trabajador = :trabajador
      AND oc.tipo_costo = :tipoCosto
    
      AND (:idOrden IS NULL OR oc.id_orden = :idOrden)

      AND (:pagado IS NULL OR oc.pagado = :pagado)

      AND (:fechaInicio IS NULL OR oc.fecha_trabajo >= :fechaInicio)
      AND (:fechaFin IS NULL OR oc.fecha_trabajo <= :fechaFin)

      AND (:idOrdenDetalle IS NULL OR oc.id_orden_detalle = :idOrdenDetalle)

    ORDER BY oc.trabajador, oc.fecha_trabajo DESC 
    """, nativeQuery = true)
  List<OrdenCosto> obtenerOrdenes(
      @Param("tipoCosto") String tipoCosto, @Param("trabajador") String trabajador, 
      @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin, 
      @Param("idOrden") Long idOrden, @Param("idOrdenDetalle") Long idOrdenDetalle, 
      @Param("pagado") Boolean pagado);


   @Query(value = """
    SELECT COALESCE(SUM(oc.cantidad_trabajada * oc.costo), 0)
    FROM orden_costo oc
    WHERE  oc.trabajador = :trabajador
      AND  oc.tipo_costo = :tipoCosto
      
      AND (:idOrden IS NULL OR oc.id_orden = :idOrden)
      AND (:pagado IS NULL OR oc.pagado = :pagado)
      AND (:fechaInicio IS NULL OR oc.fecha_trabajo >= :fechaInicio)
      AND (:fechaFin IS NULL OR oc.fecha_trabajo <= :fechaFin)
      AND (:idOrdenDetalle IS NULL OR oc.id_orden_detalle = :idOrdenDetalle)
    """, nativeQuery = true)
  BigDecimal obtenerTotalMonto(
      @Param("tipoCosto") String tipoCosto, 
      @Param("trabajador") String trabajador, 
      @Param("fechaInicio") LocalDate fechaInicio, 
      @Param("fechaFin") LocalDate fechaFin, 
      @Param("idOrden") Long idOrden, 
      @Param("idOrdenDetalle") Long idOrdenDetalle, 
      @Param("pagado") Boolean pagado);
}