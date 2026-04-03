package com.github.kraudy.InventoryBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.github.kraudy.InventoryBackend.model.ProductoMedida;
import com.github.kraudy.InventoryBackend.repository.ProductoMedidaRepository;

import java.util.List;

@RestController
@RequestMapping("/api/productos-medidas")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductoMedidaController {
  @Autowired
  private ProductoMedidaRepository productoMedidaRepository;

  @GetMapping
  public List<ProductoMedida> getAll() {
    return productoMedidaRepository.findAll();
  }

  @GetMapping("/{medida}")
  public ProductoMedida getById(@PathVariable String medida) {
    return productoMedidaRepository.findById(medida).orElse(null);
  }

  @PostMapping
  public ProductoMedida create(@RequestBody ProductoMedida productoMedida) {
    return productoMedidaRepository.save(productoMedida);
  }

  @PutMapping("/{medida}")
  public ProductoMedida update(@PathVariable String medida, @RequestBody ProductoMedida productoMedida) {
    productoMedida.setMedida(medida);
    return productoMedidaRepository.save(productoMedida);
  }

  @DeleteMapping("/{medida}")
  public void delete(@PathVariable String medida) {
    productoMedidaRepository.deleteById(medida);
  }
}

