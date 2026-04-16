package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.ProductoConfigDTO;
import com.github.kraudy.InventoryBackend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/* This thing maps product */
public interface ProductoRepository extends JpaRepository<Producto, Long> {
  
  @Query(value ="""
    SELECT id, tipo_producto, sub_tipo_producto, medida_producto, modelo_producto, nombre, descripcion, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion, activo, color_producto, imagen
    FROM producto
    WHERE (:id IS NULL OR id = :id)
      AND (:nombre IS NULL OR LOWER(nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
      AND (:tipo IS NULL OR tipo_producto = :tipo)
      AND (:subTipo IS NULL OR sub_tipo_producto = :subTipo)
      AND (:medida IS NULL OR medida_producto = :medida)
      AND (:modelo IS NULL OR modelo_producto = :modelo)
      AND (:color IS NULL OR color_producto = :color)
      AND (:sinPrecio IS NULL  OR :sinPrecio = false
               OR NOT EXISTS (
                   SELECT 1 
                   FROM producto_precio pp 
                   WHERE pp.producto_id = producto.id 
                     AND pp.activo = true
               ))
    ORDER BY id
    """, nativeQuery = true)
  List<Producto> obtenerProductos(@Param("id") Long id, @Param("nombre") String nombre, @Param("tipo") String tipo, @Param("subTipo") String subTipo, 
   @Param("medida") String medida, @Param("modelo") String modelo, @Param("color") String color, 
   @Param("sinPrecio") Boolean sinPrecio);
  
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
          AND p.medida_producto = :medida AND p.modelo_producto = :modelo and p.color_producto = :color
      """, nativeQuery = true)
  boolean existeProducto(@Param("tipo") String tipo, @Param("subTipo") String subTipo, @Param("medida") String medida, @Param("modelo") String modelo, @Param("color") String color);

  @Query(value = """
      SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END 
      FROM Producto p
      WHERE p.tipo_producto = :tipo AND p.sub_tipo_producto = :subTipo
          AND p.medida_producto = :medida AND p.modelo_producto = :modelo and p.color_producto = :color
          AND p.id <> :id
      """, nativeQuery = true)
  boolean existeProductoDiferenteId(@Param("tipo") String tipo, @Param("subTipo") String subTipo, @Param("medida") String medida, @Param("modelo") String modelo, @Param("color") String color, @Param("id") Long id);


  @Query(value = """
      SELECT tipo, sub_tipo, medida, modelo, color

      FROM producto_config
      
      WHERE (:tipo IS NULL OR tipo = :tipo)
        AND (:subTipo IS NULL OR sub_tipo = :subTipo)
        AND (:medida IS NULL OR medida = :medida)
        AND (:modelo IS NULL OR modelo = :modelo)
        AND (:color IS NULL OR color = :color)

      ORDER BY tipo, sub_tipo, medida, modelo, color
      """, nativeQuery = true)
  List<ProductoConfigDTO> obtenerConfiguracionesValidas(
      @Param("tipo") String tipo,
      @Param("subTipo") String subTipo,
      @Param("medida") String medida,
      @Param("modelo") String modelo,
      @Param("color") String color);
}