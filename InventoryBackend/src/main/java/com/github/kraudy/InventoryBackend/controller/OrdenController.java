package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.dto.OrdenDTO;
import com.github.kraudy.InventoryBackend.model.Cliente;
import com.github.kraudy.InventoryBackend.model.Orden;
import com.github.kraudy.InventoryBackend.repository.ClienteRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin(origins = "http://localhost:4200")  // Prevents Cors error. Allow Angular dev server
public class OrdenController {

  @Autowired
  private OrdenRepository ordenRepository;

  @Autowired
  private ClienteRepository clienteRepository;

  @GetMapping
  public List<OrdenDTO> getAll() {
    return ordenRepository.getAll();
  }

  @GetMapping("/{id}")
  public OrdenDTO getById(@PathVariable Long id) {
    return ordenRepository.getOrdenById(id);
  }

  @GetMapping("/recibidas")
  public List<OrdenDTO> getgetRecibidas() {
    return ordenRepository.getRecibidas();
  }

  @PostMapping
  public Orden create(@RequestBody Orden orden) {
    String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
    orden.setCreadaPor(currentUser);
    
    return ordenRepository.save(orden);
  }

  @PutMapping("/{id}")
  public Orden update(@PathVariable Long id, @RequestBody Orden orden) {
    String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
    orden.setId(id);
    orden.setModificadaPor(currentUser);
    
    return ordenRepository.save(orden);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    ordenRepository.deleteById(id);
  }

}
