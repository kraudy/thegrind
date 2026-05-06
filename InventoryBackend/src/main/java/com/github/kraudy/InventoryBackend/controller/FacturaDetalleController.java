package com.github.kraudy.InventoryBackend.controller;

import java.math.BigDecimal;

import com.github.kraudy.InventoryBackend.dto.FacturaDTO;
import com.github.kraudy.InventoryBackend.dto.FacturaDetalleDTO;
import com.github.kraudy.InventoryBackend.model.FacturaDetallePK;
import com.github.kraudy.InventoryBackend.model.ProductoPrecio;
import com.github.kraudy.InventoryBackend.model.FacturaDetalle;
import com.github.kraudy.InventoryBackend.model.ProductoPrecioPK;

import com.github.kraudy.InventoryBackend.repository.FacturaDetalleRepository;
import com.github.kraudy.InventoryBackend.repository.FacturaRepository;

import com.github.kraudy.InventoryBackend.repository.ProductoPrecioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.*;

import com.github.kraudy.InventoryBackend.service.FacturaService;
import com.github.kraudy.InventoryBackend.service.OrdenPdfService;

import java.util.List;

@RestController
@RequestMapping("/api/facturas-detalle")
@CrossOrigin(origins = "http://localhost:4200")
public class FacturaDetalleController {

  @Autowired
  private FacturaDetalleRepository facturaDetalleRepository;

  @Autowired
  private FacturaRepository facturaRepository;

  @Autowired
  private ProductoPrecioRepository productoPrecioRepository;

  @Autowired
  private FacturaService facturaDetalleService;

  // Obtener todos los detalles de una factura específica
  @GetMapping("/{idFactura}")
  public List<FacturaDetalleDTO> getByFactura(@PathVariable Long idFactura) {
    return facturaDetalleRepository.getFacturaDetalle(idFactura);
  }

  //@GetMapping("/{idFactura}/pdf")
  //public ResponseEntity<byte[]> downloadPdf(@PathVariable Long idFactura) {
  //    // Use your existing DTO query that already includes clienteNombre
  //    FacturaDTO facturaDetalleDTO = facturaRepository.getFacturaById(idFactura);
  //    if (facturaDetalleDTO == null) {
  //        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada");
  //    }

  //    List<FacturaDetalleDTO> detalles = facturaDetalleRepository.getFacturaDetalle(idFactura);

  //    byte[] pdfBytes = ordenPdfService.generateOrdenPdf(facturaDetalleDTO, detalles);
  //    HttpHeaders headers = new HttpHeaders();
  //    headers.setContentType(MediaType.APPLICATION_PDF);
  //    headers.setContentDisposition(ContentDisposition.builder("attachment")
  //            .filename("factura-" + idFactura + ".pdf")
  //            .build());
  //    return ResponseEntity.ok().headers(headers).body(pdfBytes);
  //}

  // Obtener un detalle específico por clave compuesta
  @GetMapping("/{idFactura}/{idDetalle}")
  public FacturaDetalle getById(
          @PathVariable Long idFactura,
          @PathVariable Long idDetalle){

    FacturaDetallePK pk = new FacturaDetallePK(idFactura, idDetalle);
    return facturaDetalleRepository.findById(pk).orElse(null);
  }

  // Eliminar un detalle
  @DeleteMapping("/{idFactura}/{idFacturaDetalle}")
  public void delete(
          @PathVariable Long idFactura,
          @PathVariable Long idFacturaDetalle) {

    FacturaDetallePK pk = new FacturaDetallePK(idFactura, idFacturaDetalle);
    facturaDetalleRepository.deleteById(pk);
  }
}
