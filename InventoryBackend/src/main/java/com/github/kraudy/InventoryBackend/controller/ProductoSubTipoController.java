package com.github.kraudy.InventoryBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.github.kraudy.InventoryBackend.model.ProductoSubTipo;
import com.github.kraudy.InventoryBackend.repository.ProductoSubTipoRepository;

import java.util.List;

@RestController
@RequestMapping("/api/productos-sub-tipos")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductoSubTipoController {
  @Autowired
  private ProductoSubTipoRepository productoSubTipoRepository;

  @GetMapping
  public List<ProductoSubTipo> getAll() {
    return productoSubTipoRepository.findAll();
  }

  @GetMapping("/{subTipo}")
  public ProductoSubTipo getById(@PathVariable String subTipo) {
    return productoSubTipoRepository.findById(subTipo).orElse(null);
  }

  @PostMapping
  public ProductoSubTipo create(@RequestBody ProductoSubTipo productoTipo) {
    return productoSubTipoRepository.save(productoTipo);
  }

  @PutMapping("/{subTipo}")
  public ProductoSubTipo update(@PathVariable String subTipo, @RequestBody ProductoSubTipo productoSubTipo) {
    productoSubTipo.setSubTipo(subTipo);
    return productoSubTipoRepository.save(productoSubTipo);
  }

  @DeleteMapping("/{subTipo}")
  public void delete(@PathVariable String subTipo) {
    productoSubTipoRepository.deleteById(subTipo);
  }
}
