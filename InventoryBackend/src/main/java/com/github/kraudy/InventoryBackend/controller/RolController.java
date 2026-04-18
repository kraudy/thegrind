package com.github.kraudy.InventoryBackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.github.kraudy.InventoryBackend.model.Rol;

import com.github.kraudy.InventoryBackend.repository.RolRepository;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "http://localhost:4200")
public class RolController {
  
  @Autowired
  private RolRepository rolRepository;

  /* Retorna todos los roles */
  @GetMapping
  public List<String> getAllRoles() {
    return rolRepository.getAllRoles();
  }

}
