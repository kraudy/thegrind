package com.github.kraudy.InventoryBackend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.github.kraudy.InventoryBackend.dto.UsuarioNombreDTO;
import com.github.kraudy.InventoryBackend.dto.UsuarioTrabajoDTO;
import com.github.kraudy.InventoryBackend.dto.auth.UsuarioAdminDTO;
import com.github.kraudy.InventoryBackend.dto.auth.CreateUsuarioRequest;
import com.github.kraudy.InventoryBackend.model.Usuario;
import com.github.kraudy.InventoryBackend.repository.RolRepository;
import com.github.kraudy.InventoryBackend.repository.UsuarioRepository;
import com.github.kraudy.InventoryBackend.service.CurrentUserService;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {
  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private RolRepository rolRepository;

  @Autowired
  private CurrentUserService currentUserService;

  /* Retorna todos los usuarios con su lista de roles */
  @GetMapping
  public List<UsuarioAdminDTO> getAllUsuarios() {
    return usuarioRepository.getAllUsuariosConRoles();
  }

  @GetMapping("/{usuario}")
  public UsuarioAdminDTO getByUsuario(@PathVariable String usuario) {
      if (!usuarioRepository.existsById(usuario)) {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
      }
      return usuarioRepository.getAllUsuariosConRolesPorId(usuario);
  }

  //TODO: Cambiar endpoint a reparadores/trabajo o algo asi y lo mismo para normal
  @GetMapping("/repara")
  public List<UsuarioTrabajoDTO> getReparadores() {
    return usuarioRepository.getUsuariosTrabajo("Reparacion", "repara");
  }

  @GetMapping("/normal")
  public List<UsuarioTrabajoDTO> getNormal() {
    return usuarioRepository.getUsuariosTrabajo("Normal", "normal");
  }

  @GetMapping("/pegadores/nombres")
  public List<UsuarioNombreDTO> getPegadoresNombre() {
    return usuarioRepository.getUsuarios("pega");
  }

  @GetMapping("/reparadores/nombres")
  public List<UsuarioNombreDTO> getReparadoresNombre() {
    return usuarioRepository.getUsuarios("repara");
  }

  // Crear usuario con roles
  @PostMapping
  public ResponseEntity<String> createUsuario(@RequestBody CreateUsuarioRequest req) {
    if (usuarioRepository.existsById(req.usuario())) {
        return ResponseEntity.badRequest().body("Usuario ya existe");
    }

    String currentUser = currentUserService.getCurrentUser();

    Usuario user = new Usuario();
    user.setUsuario(req.usuario());
    user.setPassword(req.password());
    user.setActivo(req.activo());
    user.setUsuarioCreacion(currentUser);
    user.setUsuarioModificacion(currentUser);
    usuarioRepository.save(user);

    for (String rol : req.roles()) {
        if (rolRepository.existsById(rol)) {
            usuarioRepository.assignRole(req.usuario(), rol);
        }
    }
    return ResponseEntity.ok("Usuario creado correctamente");
  }

  // Activar o desactivar usuario
  @PutMapping("/{usuario}/activo")
  public UsuarioAdminDTO toggleActivo(@PathVariable String usuario, @RequestBody boolean activo) {
    if (!usuarioRepository.existsById(usuario)) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
    }
    String currentUser = currentUserService.getCurrentUser();

    usuarioRepository.updateActivo(usuario, activo, currentUser);
    
    return usuarioRepository.getAllUsuariosConRolesPorId(usuario);
  }

  // Restablecer contraseña
  @PostMapping("/{usuario}/reset-password")
  public ResponseEntity<String> resetPassword(
          @PathVariable String usuario,
          @RequestBody Map<String, String> request) {

      String password = request.get("password");

      if (password == null || password.isBlank()) {
          return ResponseEntity.badRequest().body("Contraseña requerida");
      }
      if (!usuarioRepository.existsById(usuario)) {
          return ResponseEntity.notFound().build();
      }

      String currentUser = currentUserService.getCurrentUser();
      usuarioRepository.resetPassword(usuario, password, currentUser);
      
      return ResponseEntity.ok("Contraseña restablecida");
  }

  @PostMapping("/{usuario}/roles")
  public ResponseEntity<String> assignRole(@PathVariable String usuario, @RequestBody Map<String, String> request) {
    String rol = request.get("rol");
    if (!usuarioRepository.existsById(usuario) || !rolRepository.existsById(rol)) {
      return ResponseEntity.badRequest().body("Usuario o rol inválido");
    }
    usuarioRepository.assignRole(usuario, rol);
    return ResponseEntity.ok("Rol asignado");
  }

  @DeleteMapping("/{usuario}/roles/{rol}")
  public ResponseEntity<String> removeRole(@PathVariable String usuario, @PathVariable String rol) {
    usuarioRepository.removeRole(usuario, rol);
    return ResponseEntity.ok("Rol eliminado");
  }

}
