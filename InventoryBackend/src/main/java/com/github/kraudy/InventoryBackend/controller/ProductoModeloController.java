package com.github.kraudy.InventoryBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.github.kraudy.InventoryBackend.model.ProductoModelo;
import com.github.kraudy.InventoryBackend.repository.ProductoModeloRepository;

import java.util.List;

@RestController
@RequestMapping("/api/productos-modelos")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductoModeloController {
  @Autowired
  private ProductoModeloRepository productoModeloRepository;

  @GetMapping
  public List<ProductoModelo> getAll() {
    return productoModeloRepository.findAll();
  }

  @GetMapping("/{modelo}")
  public ProductoModelo getById(@PathVariable String modelo) {
    return productoModeloRepository.findById(modelo).orElse(null);
  }

  @PostMapping
  public ProductoModelo create(@RequestBody ProductoModelo productoModelo) {
    return productoModeloRepository.save(productoModelo);
  }

  @PutMapping("/{modelo}")
  public ProductoModelo update(@PathVariable String modelo, @RequestBody ProductoModelo productoModelo) {
    productoModelo.setModelo(modelo);
    return productoModeloRepository.save(productoModelo);
  }

  @DeleteMapping("/{modelo}")
  public void delete(@PathVariable String modelo) {
    productoModeloRepository.deleteById(modelo);
  }
}
