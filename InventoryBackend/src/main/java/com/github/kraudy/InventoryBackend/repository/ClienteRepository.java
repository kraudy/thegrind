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
}
