package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.UsuarioDTO;
import com.github.kraudy.InventoryBackend.model.Usuario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

  /* Obtiene listado de usuarios por rol */

  @Query(value = """
    SELECT
        usr.usuario

    FROM usuario usr
    JOIN usuario_rol usrRol ON usr.usuario = usrRol.usuario
    WHERE usrRol.rol = 'repara'
    """, nativeQuery = true)
  List<UsuarioDTO> getUsuariosReparacion();

  @Query(value = """
    SELECT
        usr.usuario

    FROM usuario usr
    JOIN usuario_rol usrRol ON usr.usuario = usrRol.usuario
    WHERE usrRol.rol = 'normal'
    """, nativeQuery = true)
  List<UsuarioDTO> getUsuariosNormal();

  @Query(value = """
    SELECT
        usr.usuario

    FROM usuario usr
    JOIN usuario_rol usrRol ON usr.usuario = usrRol.usuario
    WHERE usrRol.rol = 'pega'
    """, nativeQuery = true)
  List<UsuarioDTO> getUsuariosPegadores();

  /* Valida roles */

  @Query(value = """
    SELECT COUNT(*) = 1
    FROM usuario usr
    JOIN usuario_rol usrRol ON usr.usuario = usrRol.usuario
    WHERE usrRol.rol = 'repara' 
      and usr.usuario = :usuario
    """, nativeQuery = true)
  boolean usuarioEsReparador(String usuario);

  @Query(value = """
    SELECT COUNT(*) = 1
    FROM usuario usr
    JOIN usuario_rol usrRol ON usr.usuario = usrRol.usuario
    WHERE usrRol.rol = 'normal' 
      and usr.usuario = :usuario
    """, nativeQuery = true)
  boolean usuarioEsNormal(String usuario);

  @Query(value = """
    SELECT COUNT(*) = 1
    FROM usuario usr
    JOIN usuario_rol usrRol ON usr.usuario = usrRol.usuario
    WHERE usrRol.rol = 'pega' 
      and usr.usuario = :usuario
    """, nativeQuery = true)
  boolean usuarioEsPegador(String usuario);
}

