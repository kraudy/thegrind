package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.OrdenTrabajo;
import com.github.kraudy.InventoryBackend.model.OrdenTrabajoPK;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, OrdenTrabajoPK> {}
