package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.UsuarioDTO;
import com.github.kraudy.InventoryBackend.dto.UsuarioTrabajoDTO;
import com.github.kraudy.InventoryBackend.model.Usuario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

  /* Obtiene listado de usuarios por rol */

  @Query(value = """
    SELECT
        usr.usuario,
        SUM(COALESCE(ordTrab.cantidad_asignada,0)) as cantidadAsignada,
        SUM(COALESCE(ordTrab.cantidad_trabajada,0)) as cantidadTrabajada

    FROM usuario usr
    JOIN usuario_rol usrRol ON usr.usuario = usrRol.usuario
    LEFT JOIN orden_trabajo ordTrab  -- Permite mostrar reparadores sin trabajos asignados
      ON usr.usuario = ordTrab.trabajador AND ordTrab.estado = 'Reparacion'

    WHERE usrRol.rol = 'repara'
    GROUP BY usr.usuario
    """, nativeQuery = true)
  List<UsuarioTrabajoDTO> getUsuariosReparacion();

  @Query(value = """
    SELECT
        usr.usuario,
        SUM(COALESCE(ordTrab.cantidad_asignada,0)) as cantidadAsignada,
        SUM(COALESCE(ordTrab.cantidad_trabajada,0)) as cantidadTrabajada

    FROM usuario usr
    JOIN usuario_rol usrRol ON usr.usuario = usrRol.usuario
    LEFT JOIN orden_trabajo ordTrab  -- Permite mostrar normales sin trabajos asignados
      ON usr.usuario = ordTrab.trabajador AND ordTrab.estado = 'Normal'

    WHERE usrRol.rol = 'normal'
    GROUP BY usr.usuario
    """, nativeQuery = true)
  List<UsuarioTrabajoDTO> getUsuariosNormal();

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


  @Query(value = """
    SELECT COUNT(*) = 1
    FROM usuario usr
    JOIN usuario_rol usrRol ON usr.usuario = usrRol.usuario
    WHERE usrRol.rol = 'entrega' 
      and usr.usuario = :usuario
    """, nativeQuery = true)
  boolean usuarioEntrega(String usuario);

  /* NEW: Returns list of roles for a user (native query) */
  @Query(value = """
    SELECT usrRol.rol
    FROM usuario_rol usrRol
    WHERE usrRol.usuario = :usuario
    """, nativeQuery = true)
  List<String> getRolesByUsuario(@Param("usuario") String usuario);

}

