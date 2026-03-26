package com.github.kraudy.InventoryBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.github.kraudy.InventoryBackend.model.OrdenCalendarioHistorico;

public interface OrdenCalendarioHistoricoRepository extends JpaRepository<OrdenCalendarioHistorico, Long> {
  
}
