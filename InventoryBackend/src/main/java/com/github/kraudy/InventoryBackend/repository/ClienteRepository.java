package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.Cliente;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
  @Query("""
      SELECT c FROM Cliente c
      WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :term, '%'))
          OR (c.apellido IS NOT NULL AND LOWER(c.apellido) LIKE LOWER(CONCAT('%', :term, '%')))
          OR CAST(c.id AS string) LIKE CONCAT('%', :term, '%')
      LIMIT 10
      """)
  List<Cliente> searchByTerm(@Param("term") String term);

  @Query("""
      SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cliente c
      WHERE LOWER(c.nombre) = LOWER(:nombre) AND LOWER(c.apellido) = LOWER(:apellido)
      """)
  boolean existeCliente(@Param("nombre") String nombre, @Param("apellido") String apellido);

  @Query("""
      SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cliente c
      WHERE c.id <> :id AND 
        LOWER(c.nombre) = LOWER(:nombre) AND LOWER(c.apellido) = LOWER(:apellido) 
      """)
  boolean existeClienteDiferenteId(@Param("nombre") String nombre, @Param("apellido") String apellido, @Param("id") Long id);

}
