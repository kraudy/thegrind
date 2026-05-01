package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.dto.OrdenCostoDTO;
import com.github.kraudy.InventoryBackend.model.OrdenCosto;
import com.github.kraudy.InventoryBackend.repository.OrdenCostoRepository;
import com.github.kraudy.InventoryBackend.service.OrdenCostoService;
import com.github.kraudy.InventoryBackend.service.OrdenPdfService;
import com.github.kraudy.InventoryBackend.service.pdf.CostoPdfService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.*;

@RestController
@RequestMapping("/api/ordenes-costo")
@CrossOrigin(origins = "http://localhost:4200")  // Prevents Cors error. Allow Angular dev server
public class OrdenCostoController {
  
  @Autowired
  private OrdenCostoRepository ordenCostoRepository;
  
  @Autowired
  private OrdenCostoService ordenCostoService;

  @Autowired
  private CostoPdfService costoPdfService;

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

  @GetMapping("/recibo/{tipoCosto}/{trabajador}")
  public ResponseEntity<byte[]> generarReciboPDF(
          @PathVariable String tipoCosto,
          @PathVariable String trabajador,
          @RequestParam(required = false) LocalDate fechaInicio,
          @RequestParam(required = false) LocalDate fechaFin,
          @RequestParam(required = false) Long idOrden,
          @RequestParam(required = false) Long idOrdenDetalle) {

      // Reuse the same query you already have
      List<OrdenCostoDTO> costos = ordenCostoRepository.obtenerOrdenesDTO(tipoCosto, trabajador, fechaInicio, fechaFin, idOrden, idOrdenDetalle, null);
      BigDecimal total = ordenCostoRepository.obtenerTotalMonto(tipoCosto, trabajador, fechaInicio, fechaFin, idOrden, idOrdenDetalle, null);

      byte[] pdfBytes = costoPdfService.generateCostoPdf(costos, tipoCosto, trabajador, total);

      return ResponseEntity.ok()
              .header("Content-Disposition", "attachment; filename=Recibo-Pago-" + trabajador + "-" + LocalDate.now() + ".pdf")
              .contentType(MediaType.APPLICATION_PDF)
              .body(pdfBytes);
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
