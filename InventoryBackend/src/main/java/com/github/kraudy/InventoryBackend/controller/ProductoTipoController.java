package com.github.kraudy.InventoryBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.github.kraudy.InventoryBackend.model.ProductoTipo;
import com.github.kraudy.InventoryBackend.repository.ProductoTipoRepository;

import java.util.List;

@RestController
@RequestMapping("/api/productos-tipo")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductoTipoController {
  @Autowired
  private ProductoTipoRepository productoTipoRepository;

  @GetMapping
  public List<ProductoTipo> getAll() {
    return productoTipoRepository.findAll();
  }

  @GetMapping("/{tipo}")
  public ProductoTipo getById(@PathVariable String tipo) {
    return productoTipoRepository.findById(tipo).orElse(null);
  }

  @PostMapping
  public ProductoTipo create(@RequestBody ProductoTipo productoTipo) {
    return productoTipoRepository.save(productoTipo);
  }

  @PutMapping("/{tipo}")
  public ProductoTipo update(@PathVariable String tipo, @RequestBody ProductoTipo productoTipo) {
    productoTipo.setTipo(tipo);
    return productoTipoRepository.save(productoTipo);
  }

  @DeleteMapping("/{tipo}")
  public void delete(@PathVariable String tipo) {
    productoTipoRepository.deleteById(tipo);
  }
}
