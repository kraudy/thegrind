package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/* This thing maps product */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

  //@Query("SELECT p FROM Producto p " +
  //        "WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :term, '%')) " +
  //        "OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :term, '%')) " +
  //        "OR CAST(p.id AS string) LIKE CONCAT('%', :term, '%')")

  //@Query(
  //  "SELECT p " +
  //  "FROM Producto p " +
  //  "Where Cast(p.id as Varchar (10)) || ' ' || p.nombre || ' ' || p.descripcion Like '%' || :term || '%'")

  //@Query(
  //  "SELECT p " +
  //  "FROM Producto p " +
  //  "Where Cast(p.id as string) || ' ' || p.nombre || ' ' || p.descripcion Like '%' || :term || '%'")
  
  @Query("""
      SELECT p FROM Producto p
      WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :term, '%'))
          OR (p.descripcion IS NOT NULL AND LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :term, '%')))
          OR CAST(p.id AS string) LIKE CONCAT('%', :term, '%')
      """)
  List<Producto> searchByTerm(@Param("term") String term);
}