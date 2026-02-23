package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.ProductoTipoEstado;
import com.github.kraudy.InventoryBackend.model.ProductoTipoEstadoPK;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductoTipoEstadoRepository extends JpaRepository<ProductoTipoEstado, ProductoTipoEstadoPK> {
  List<ProductoTipoEstado> findByTipoAndSubTipoOrderBySecuenciaAsc(String tipo, String subTipo);
}

