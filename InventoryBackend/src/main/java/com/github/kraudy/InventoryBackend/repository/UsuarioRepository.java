package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.auth.UsuarioAdminDTO
;
import com.github.kraudy.InventoryBackend.dto.UsuarioNombreDTO;
import com.github.kraudy.InventoryBackend.dto.UsuarioTrabajoDTO;
import com.github.kraudy.InventoryBackend.model.Usuario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

  /* Obtiene listado de usuarios por rol */

  @Query(value = """
    With cantidades AS (
      SELECT 
        trab.trabajador,
        SUM(COALESCE(trab.cantidad_asignada,0)) as cantidadAsignada,
        SUM(COALESCE(trab.cantidad_trabajada,0)) as cantidadTrabajada 
      FROM orden_calendario cal

      INNER JOIN orden_seguimiento seg 
        ON (cal.id_orden = seg.id_orden AND
            seg.estado = :estado)      -- Queremos solo las  que quedaron en estado normal, no que pasaron por normal

      INNER JOIN orden_trabajo trab 
        ON (seg.id_orden = trab.id_orden AND
            seg.id_orden_detalle = trab.id_orden_detalle AND
            trab.estado = seg.estado)

      WHERE cal.fecha = CURRENT_DATE

      GROUP BY trab.trabajador
    )
    SELECT
        usr.usuario,
        COALESCE(cantidades.cantidadAsignada,0) as cantidadAsignada,
        COALESCE(cantidades.cantidadTrabajada,0) as cantidadTrabajada

    FROM usuario usr
    JOIN usuario_rol usrRol 
      ON (usr.usuario = usrRol.usuario AND 
          usrRol.rol = :rol)

    LEFT JOIN cantidades  -- Permite mostrar usuarios sin trabajos asignados
      ON (usr.usuario = cantidades.trabajador)

    """, nativeQuery = true)
  List<UsuarioTrabajoDTO> getUsuariosTrabajo(@Param("estado") String estado, @Param("rol") String rol);

  @Query(value = """
    SELECT
        usr.usuario

    FROM usuario usr
    JOIN usuario_rol usrRol ON usr.usuario = usrRol.usuario
    WHERE usrRol.rol = 'pega'
    """, nativeQuery = true)
  List<UsuarioNombreDTO> getUsuariosPegadores();

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

  /* Returns list of roles for a user */
  @Query(value = """
    SELECT usrRol.rol
    FROM usuario_rol usrRol
    WHERE usrRol.usuario = :usuario
    """, nativeQuery = true)
  List<String> getRolesByUsuario(@Param("usuario") String usuario);

  // ====================== ACTIVO & PASSWORD ======================

  @Query(value = """
    SELECT u.usuario,
           u.activo,
           COALESCE(ARRAY_AGG(ur.rol), ARRAY[]::text[]) as roles
    FROM usuario u
    LEFT JOIN usuario_rol ur ON u.usuario = ur.usuario
    GROUP BY u.usuario, u.activo
    """, nativeQuery = true)
  List<UsuarioAdminDTO> getAllUsuariosConRoles();

  @Query(value = """
    SELECT u.usuario,
           u.activo,
           COALESCE(ARRAY_AGG(ur.rol), ARRAY[]::text[]) as roles
    FROM usuario u
    LEFT JOIN usuario_rol ur ON u.usuario = ur.usuario

    WHERE u.usuario = :usuario
    GROUP BY u.usuario, u.activo
    """, nativeQuery = true)
  UsuarioAdminDTO getAllUsuariosConRolesPorId(@Param("usuario") String usuario);

  @Modifying
  @Transactional
  @Query(value = """
      INSERT INTO usuario_rol (usuario, rol) 
      VALUES (:usuario, :rol) ON CONFLICT DO NOTHING
      """, nativeQuery = true)
  void assignRole(@Param("usuario") String usuario, @Param("rol") String rol);

  @Modifying
  @Transactional
  @Query(value = """
      DELETE FROM usuario_rol 
      WHERE usuario = :usuario AND 
            rol = :rol
      """, nativeQuery = true)
  void removeRole(@Param("usuario") String usuario, @Param("rol") String rol);

  // ====================== ACTIVO & PASSWORD ======================
  @Modifying
  @Transactional
  @Query(value = """
      UPDATE usuario 
      SET activo = :activo, 
          fecha_modificacion = CURRENT_TIMESTAMP, 
          usuario_modificacion = :usuarioModificacion

      WHERE usuario = :usuario
      """, nativeQuery = true)
  void updateActivo(@Param("usuario") String usuario, @Param("activo") boolean activo, @Param("usuarioModificacion") String usuarioModificacion);

  @Modifying
  @Transactional
  @Query(value = """
      UPDATE usuario 
      SET password = :password, 
          fecha_modificacion = CURRENT_TIMESTAMP, 
          usuario_modificacion = :usuarioModificacion

      WHERE usuario = :usuario
      """, nativeQuery = true)
  void resetPassword(@Param("usuario") String usuario, @Param("password") String password, @Param("usuarioModificacion") String usuarioModificacion);

}

