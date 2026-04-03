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
  
  @Query(value ="""
    SELECT id, tipo_producto, sub_tipo_producto, medida_producto, modelo_producto, nombre, descripcion, fecha_creacion, fecha_modificacion, activo
    FROM producto
    WHERE (:id IS NULL OR id = :id)
      AND (:nombre IS NULL OR LOWER(nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
      AND (:tipo IS NULL OR tipo_producto = :tipo)
      AND (:subTipo IS NULL OR sub_tipo_producto = :subTipo)
    ORDER BY id
    """, nativeQuery = true)
  List<Producto> obtenerProductos(@Param("id") Long id, @Param("nombre") String nombre, @Param("tipo") String tipo, @Param("subTipo") String subTipo);
  
  @Query(value ="""
    SELECT p FROM Producto p
    WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :term, '%'))
        OR (p.descripcion IS NOT NULL AND LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :term, '%')))
        OR CAST(p.id AS string) LIKE CONCAT('%', :term, '%')
    """, nativeQuery = true)
  List<Producto> searchByTerm(@Param("term") String term);

  @Query(value ="""
      SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Producto p
      WHERE p.tipo_producto = :tipo AND p.sub_tipo_producto = :subTipo
          AND p.medida_producto = :medida AND p.modelo_producto = :modelo
      """, nativeQuery = true)
  boolean existeProducto(@Param("tipo") String tipo, @Param("subTipo") String subTipo, @Param("medida") String medida, @Param("modelo") String modelo);

  @Query(value = """
      SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END 
      FROM Producto p
      WHERE p.tipo_producto = :tipo AND p.sub_tipo_producto = :subTipo
          AND p.medida_producto = :medida AND p.modelo_producto = :modelo
          AND p.id <> :id
      """, nativeQuery = true)
  boolean existeProductoDiferenteId(@Param("tipo") String tipo, @Param("subTipo") String subTipo, @Param("medida") String medida, @Param("modelo") String modelo, @Param("id") Long id);
}