package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.dto.OrdenCostoDTO;
import com.github.kraudy.InventoryBackend.repository.OrdenCostoRepository;
import com.github.kraudy.InventoryBackend.service.OrdenCostoService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ordenes-costo")
@CrossOrigin(origins = "http://localhost:4200")  // Prevents Cors error. Allow Angular dev server
public class OrdenCostoController {
  
  @Autowired
  private OrdenCostoRepository ordenCostoRepository;
  
  @Autowired
  private OrdenCostoService ordenCostoService;

  @GetMapping("/{tipoCosto}/{trabajador}")
  public List<OrdenCostoDTO> getAll(
          @PathVariable String tipoCosto,
          @PathVariable String trabajador, 
          @RequestParam(required = false) LocalDate fechaInicio,
          @RequestParam(required = false) LocalDate fechaFin,
          @RequestParam(required = false) Long idOrden,
          @RequestParam(required = false) Long idOrdenDetalle,
          @RequestParam(required = false) Boolean pagado
  ) {
    return ordenCostoRepository.obtenerOrdenesDTO(tipoCosto, trabajador, fechaInicio, fechaFin, idOrden, idOrdenDetalle, pagado);
  }

  @GetMapping("/total/{tipoCosto}/{trabajador}")
  public BigDecimal getTotal(
          @PathVariable String tipoCosto,
          @PathVariable String trabajador, 
          @RequestParam(required = false) LocalDate fechaInicio,
          @RequestParam(required = false) LocalDate fechaFin,
          @RequestParam(required = false) Long idOrden,
          @RequestParam(required = false) Long idOrdenDetalle,
          @RequestParam(required = false) Boolean pagado
  ) {
    return ordenCostoRepository.obtenerTotalMonto(tipoCosto, trabajador, fechaInicio, fechaFin, idOrden, idOrdenDetalle, pagado);
  }

  @PostMapping("/pagar/{tipoCosto}/{trabajador}")
  public void pagarCosto(
          @PathVariable String tipoCosto,
          @PathVariable String trabajador, 
          @RequestParam(required = false) LocalDate fechaInicio,
          @RequestParam(required = false) LocalDate fechaFin,
          @RequestParam(required = false) Long idOrden,
          @RequestParam(required = false) Long idOrdenDetalle) {

    ordenCostoService.pagarOrdenCosto(tipoCosto, trabajador, fechaInicio, fechaFin, idOrden, idOrdenDetalle);
  }

}
