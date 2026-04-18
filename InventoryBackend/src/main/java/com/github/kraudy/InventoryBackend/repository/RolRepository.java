package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.auth.UsuarioAdminDTO;
import com.github.kraudy.InventoryBackend.model.Rol;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RolRepository extends JpaRepository<Rol, String> {

  @Query(value = """
    SELECT COALESCE(ARRAY_AGG(r.rol), ARRAY[]::text[]) as roles
    
    FROM rol r

    --WHERE r.activo = true
    
    GROUP BY r.rol
    """, nativeQuery = true)
  List<String> getAllRoles();

}

