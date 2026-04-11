package com.github.kraudy.InventoryBackend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.github.kraudy.InventoryBackend.model.ProductoColor;
import com.github.kraudy.InventoryBackend.repository.ProductoColorRepository;

import java.util.List;

@RestController
@RequestMapping("/api/productos-colores")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductoColorController {
  @Autowired
  private ProductoColorRepository productoColorRepository;

  @GetMapping
  public List<ProductoColor> getAll() {
    return productoColorRepository.findAll();
  }

  @GetMapping("/{color}")
  public ProductoColor getById(@PathVariable String color) {
    return productoColorRepository.findById(color).orElse(null);
  }

  @PostMapping
  public ProductoColor create(@RequestBody ProductoColor productoColor) {
    return productoColorRepository.save(productoColor);
  }

  @PutMapping("/{color}")
  public ProductoColor update(@PathVariable String color, @RequestBody ProductoColor productoColor) {
    productoColor.setColor(color);
    return productoColorRepository.save(productoColor);
  }

  @DeleteMapping("/{color}")
  public void delete(@PathVariable String color) {
    productoColorRepository.deleteById(color);
  }
}

