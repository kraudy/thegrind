package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.OrdenCalendarioDTO;
import com.github.kraudy.InventoryBackend.model.OrdenCalendario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrdenCalendarioRepository extends JpaRepository<OrdenCalendario, Long> {

  @Query(value ="""
    SELECT
        cal.id_orden,
        cal.fecha_trabajo,
        EXTRACT(DATE FROM cal.fecha_trabajo) AS fecha,
        EXTRACT(DAY FROM cal.fecha_trabajo) AS diaTrabajo,
        EXTRACT(HOUR FROM cal.fecha_trabajo) AS horaTrabajo,
        EXTRACT(MINUTE FROM cal.fecha_trabajo) AS minutoTrabajo,
        cal.fecha_creacion,
        cal.usuario_crea,
        cal.fecha_modificacion,
        cal.usuario_modifica

    FROM orden_calendario cal
    ORDER BY cal.id_orden ASC
  """, nativeQuery = true)
  List<OrdenCalendarioDTO> getAllOrdenCalendario();

  @Query(value ="""
    SELECT
        cal.id_orden,
        cal.fecha_trabajo,
        EXTRACT(DATE FROM cal.fecha_trabajo) AS fecha,
        EXTRACT(DAY FROM cal.fecha_trabajo) AS diaTrabajo,
        EXTRACT(HOUR FROM cal.fecha_trabajo) AS horaTrabajo,
        EXTRACT(MINUTE FROM cal.fecha_trabajo) AS minutoTrabajo,
        cal.fecha_creacion,
        cal.usuario_crea,
        cal.fecha_modificacion,
        cal.usuario_modifica

    FROM orden_calendario cal
    WHERE cal.id_orden = :idOrden
    ORDER BY cal.id_orden ASC
  """, nativeQuery = true)
  OrdenCalendarioDTO getByIdOrdenCalendario(Long idOrden);
}
