package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.model.ProductoPrecio;
import com.github.kraudy.InventoryBackend.model.ProductoPrecioPK;
import com.github.kraudy.InventoryBackend.repository.ProductoPrecioRepository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/productos-precio")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductoPrecioController {
  @Autowired
  private ProductoPrecioRepository productoPrecioRepository;

  @GetMapping
  public List<ProductoPrecio> getAll() {
      return productoPrecioRepository.findAll();
  }

  @GetMapping("/{productoId}/{precio}")
  public ProductoPrecio getById(
          @PathVariable Long productoId,
          @PathVariable BigDecimal precio) {
      ProductoPrecioPK pk = new ProductoPrecioPK(productoId, precio);
      return productoPrecioRepository.findById(pk)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Precio no encontrado"));
  }

  @PostMapping
  public ProductoPrecio create(@RequestBody ProductoPrecio productoPrecio) {
      return productoPrecioRepository.save(productoPrecio);
  }

  @PutMapping("/{productoId}/{precio}")
  public ProductoPrecio update(
          @PathVariable Long productoId,
          @PathVariable BigDecimal precio,
          @RequestBody ProductoPrecio updates) {

      ProductoPrecioPK pk = new ProductoPrecioPK(productoId, precio);
      ProductoPrecio existing = productoPrecioRepository.findById(pk)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Precio no encontrado"));

      existing.setDescripcion(updates.getDescripcion());
      existing.setCantidadRequerida(updates.getCantidadRequerida());
      existing.setActivo(updates.isActivo());

      return productoPrecioRepository.save(existing);
  }

  @DeleteMapping("/{productoId}/{precio}")
  public void delete(
          @PathVariable Long productoId,
          @PathVariable BigDecimal precio) {
      ProductoPrecioPK pk = new ProductoPrecioPK(productoId, precio);
      if (!productoPrecioRepository.existsById(pk)) {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Precio no encontrado");
      }
      productoPrecioRepository.deleteById(pk);
  }
}
