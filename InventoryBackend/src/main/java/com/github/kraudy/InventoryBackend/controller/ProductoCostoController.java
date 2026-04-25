package com.github.kraudy.InventoryBackend.controller;


import com.github.kraudy.InventoryBackend.model.ProductoCosto;
import com.github.kraudy.InventoryBackend.model.ProductoCostoPK;
import com.github.kraudy.InventoryBackend.repository.ProductoCostoRepository;
import com.github.kraudy.InventoryBackend.service.CurrentUserService;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/productos-costos")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductoCostoController {
  @Autowired
  private ProductoCostoRepository productoCostoRepository;

  @Autowired
  private CurrentUserService currentUserService;

  @GetMapping("/{idProducto}")
  public List<ProductoCosto> getByProducto(
          @PathVariable Long idProducto) {
    return productoCostoRepository.getAllProductoCostos(idProducto);
  }

  @GetMapping("/{productoId}/{tipoCosto}")
  public ProductoCosto getByProductoTipoCosto(
          @PathVariable Long productoId,
          @PathVariable String tipoCosto) {
    ProductoCostoPK pk = new ProductoCostoPK(productoId, tipoCosto);
    return productoCostoRepository.findById(pk)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Costo no encontrado"));
  }

  @PostMapping
  public ProductoCosto create(@RequestBody ProductoCosto productoCosto) {
    String currentUser = currentUserService.getCurrentUser();
    productoCosto.setUsuarioCreacion(currentUser);
    productoCosto.setUsuarioModificacion(currentUser);
      
    return productoCostoRepository.save(productoCosto);
  }

  @PutMapping("/{productoId}/{tipoCosto}")
  public ProductoCosto update(
          @PathVariable Long productoId,
          @PathVariable String tipoCosto,
          @RequestBody ProductoCosto updates) {

      ProductoCostoPK pk = new ProductoCostoPK(productoId, tipoCosto);
      ProductoCosto existing = productoCostoRepository.findById(pk)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Costo no encontrado"));

      existing.setDescripcion(updates.getDescripcion());
      existing.setCantidadRequerida(updates.getCantidadRequerida());
      existing.setActivo(updates.isActivo());
      existing.setUsuarioModificacion(currentUserService.getCurrentUser());
      
      return productoCostoRepository.save(existing);
  }

  @DeleteMapping("/{productoId}/{tipoCosto}")
  public void delete(
          @PathVariable Long productoId,
          @PathVariable String tipoCosto) {
      ProductoCostoPK pk = new ProductoCostoPK(productoId, tipoCosto);
            
      if (!productoCostoRepository.existsById(pk)) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Costo no encontrado");
      }
      productoCostoRepository.deleteById(pk);
  }
}
