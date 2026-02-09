package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.model.Orden;
import com.github.kraudy.InventoryBackend.repository.OrdenRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin(origins = "http://localhost:4200")  // Prevents Cors error. Allow Angular dev server
public class OrdenController {

  @Autowired
  private OrdenRepository ordenRepository;

  @GetMapping
  public List<Orden> getAll() {
    return ordenRepository.findAll();
  }

  @GetMapping("/{id}")
  public Orden getById(@PathVariable Long id) {
    return ordenRepository.findById(id).orElse(null);
  }

  @PostMapping
  public Orden create(@RequestBody Orden orden) {
    return ordenRepository.save(orden);
  }

  @PutMapping("/{id}")
  public Orden update(@PathVariable Long id, @RequestBody Orden orden) {
    orden.setId(id);
    return ordenRepository.save(orden);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    ordenRepository.deleteById(id);
  }
}
