package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.OrdenCosto;
import com.github.kraudy.InventoryBackend.model.OrdenCostoPK;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface OrdenCostoRepository extends JpaRepository<OrdenCosto, OrdenCostoPK> {
  @Query(value = """
    SELECT 
      oc.id_orden AS idOrden,
      oc.id_orden_detalle AS idOrdenDetalle,
      oc.tipo_costo AS tipoCosto,
      oc.trabajador AS trabajador,
      oc.rol AS rol,
      oc.id_producto AS idProducto,
      oc.cantidad_orden AS cantidadOrden,
      oc.cantidad_trabajada AS cantidadTrabajada,
      oc.pagado AS pagado,
      oc.usuario_paga AS usuarioPaga,
      oc.fecha_pago AS fechaPago,
      oc.comentario AS comentario,
      oc.fecha_trabajo AS fechaTrabajo

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