package com.github.kraudy.InventoryBackend.controller;


import com.github.kraudy.InventoryBackend.dto.FacturaDTO;
import com.github.kraudy.InventoryBackend.model.Factura;

import com.github.kraudy.InventoryBackend.repository.FacturaRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenSeguimientoRepository;
import com.github.kraudy.InventoryBackend.service.FacturaService;
import com.github.kraudy.InventoryBackend.service.OrdenSeguimientoService;

import lombok.RequiredArgsConstructor;

import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetalleFacturacionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@CrossOrigin(origins = "http://localhost:4200")  // Prevents Cors error. Allow Angular dev server
@RequiredArgsConstructor
public class FacturaController {

  @Autowired
  private FacturaRepository facturaRepository;

  @Autowired
  private OrdenSeguimientoRepository ordenSeguimientoRepository;

  private final FacturaService facturaService;


  @GetMapping
  public List<FacturaDTO> get(
    @RequestParam(required = false) Long id,
    @RequestParam(required = false) String cliente,
    @RequestParam(required = false) String facturador,
    @RequestParam(required = false) String estado
  ) {
    return facturaRepository.obtenerFacturas(id, cliente, facturador, estado);
  }

  @PostMapping("/{idOrden}")
  public Factura create(@PathVariable Long idOrden) {
    return facturaService.crearFactura(idOrden);
  }

  @PutMapping("/{id}")
  public Factura update(@PathVariable Long id, @RequestBody Factura factura) {
    String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
    factura.setId(id);
    factura.setUsuarioCreacion(currentUser);
    
    return facturaRepository.save(factura);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    facturaRepository.deleteById(id);
  }

}
