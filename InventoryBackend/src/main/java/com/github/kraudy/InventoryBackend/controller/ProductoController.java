package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.model.Producto;
import com.github.kraudy.InventoryBackend.model.ProductoPrecio;
import com.github.kraudy.InventoryBackend.repository.ProductoPrecioRepository;
import com.github.kraudy.InventoryBackend.repository.ProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "http://localhost:4200")  // Prevents Cors error. Allow Angular dev server
// For the CORS, a stand alone class could be set
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProductoPrecioRepository productoPrecioRepository;

    @GetMapping
    public List<Producto> getAll() {
        return productoRepository.findAll();
    }

    @GetMapping("/{id}")
    public Producto getById(@PathVariable Long id) {
        return productoRepository.findById(id).orElseThrow();
    }

    @PostMapping
    public Producto create(@RequestBody Producto product) {
        return productoRepository.save(product);
    }

    @PutMapping("/{id}")
    public Producto update(@PathVariable Long id, @RequestBody Producto product) {
      product.setId(id);
      return productoRepository.save(product);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
      productoRepository.deleteById(id);
    }

    @GetMapping("/{id}/precios")
    public List<ProductoPrecio> getPreciosByProductoId(@PathVariable Long id) {
      // Optional: verify the product exists first
      if (!productoRepository.existsById(id)) {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
      }
      return productoPrecioRepository.findByProductoIdOrderByFechaCreacionDesc(id);
    }
}

