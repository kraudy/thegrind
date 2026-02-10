package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.model.Producto;
import com.github.kraudy.InventoryBackend.repository.ProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:4200")  // Prevents Cors error. Allow Angular dev server
// For the CORS, a stand alone class could be set
public class ProductoController {

    @Autowired
    private ProductoRepository productRepository;

    @GetMapping
    public List<Producto> getAll() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public Producto getById(@PathVariable Long id) {
        return productRepository.findById(id).orElseThrow();
    }

    @PostMapping
    public Producto create(@RequestBody Producto product) {
        return productRepository.save(product);
    }

    @PutMapping("/{id}")
    public Producto update(@PathVariable Long id, @RequestBody Producto product) {
      product.setId(id);
      return productRepository.save(product);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productRepository.deleteById(id);
    }
}

