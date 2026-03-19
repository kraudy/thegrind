package com.github.kraudy.InventoryBackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.github.kraudy.InventoryBackend.dto.UsuarioDTO;
import com.github.kraudy.InventoryBackend.dto.UsuarioTrabajoDTO;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimiento;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoPK;
import com.github.kraudy.InventoryBackend.repository.OrdenRepository;
import com.github.kraudy.InventoryBackend.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {
  @Autowired
  private UsuarioRepository usuarioRepository;

  @GetMapping("/repara")
  public List<UsuarioTrabajoDTO> getReparadores() {
    return usuarioRepository.getUsuariosReparacion();
  }

  @GetMapping("/normal")
  public List<UsuarioTrabajoDTO> getNormal() {
    return usuarioRepository.getUsuariosNormal();
  }

  @GetMapping("/pegadores")
  public List<UsuarioDTO> getPegadores() {
    return usuarioRepository.getUsuariosPegadores();
  }

}
