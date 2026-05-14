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

  @Query(value = """
      SELECT id, nombre, apellido, telefono, correo, direccion, fecha_creacion, fecha_modificacion
      FROM cliente
      WHERE (:id IS NULL OR id = :id)
        AND (:nombre IS NULL
             OR LOWER(nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))
             OR LOWER(apellido) LIKE LOWER(CONCAT('%', :nombre, '%'))
             OR LOWER(CONCAT(nombre, ' ', COALESCE(apellido, ''))) LIKE LOWER(CONCAT('%', :nombre, '%'))
             OR LOWER(CONCAT(COALESCE(apellido, ''), ' ', nombre)) LIKE LOWER(CONCAT('%', :nombre, '%')))
        AND (:telefono IS NULL OR telefono LIKE CONCAT('%', :telefono, '%'))
        AND (:correo IS NULL OR LOWER(correo) LIKE LOWER(CONCAT('%', :correo, '%')))
      ORDER BY id

      LIMIT 50
      """, nativeQuery = true)
  List<Cliente> obtenerClientes(@Param("id") Long id,
                                @Param("nombre") String nombre,
                                @Param("telefono") String telefono,
                                @Param("correo") String correo);

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
