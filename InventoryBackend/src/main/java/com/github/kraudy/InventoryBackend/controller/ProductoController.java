package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.dto.ProductoBulkPricingRequest;
import com.github.kraudy.InventoryBackend.dto.ProductoBulkPricingResponse;
import com.github.kraudy.InventoryBackend.dto.ProductoBulkRequest;
import com.github.kraudy.InventoryBackend.dto.ProductoBulkResponse;
import com.github.kraudy.InventoryBackend.dto.ProductoConfigDTO;
import com.github.kraudy.InventoryBackend.dto.ProductoListDTO;
import com.github.kraudy.InventoryBackend.model.Producto;
import com.github.kraudy.InventoryBackend.model.ProductoCosto;
import com.github.kraudy.InventoryBackend.model.ProductoPrecio;
import com.github.kraudy.InventoryBackend.repository.ProductoCostoRepository;
import com.github.kraudy.InventoryBackend.repository.ProductoPrecioRepository;
import com.github.kraudy.InventoryBackend.repository.ProductoRepository;
import com.github.kraudy.InventoryBackend.service.ProductoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "http://localhost:4200")  // Prevents Cors error. Allow Angular dev server
// For the CORS, a stand alone class could be set
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProductoPrecioRepository productoPrecioRepository;

    @Autowired
    private ProductoCostoRepository productoCostoRepository;

    @Autowired
    private ProductoService productoService; 

    @GetMapping
    public List<ProductoListDTO> getAll(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) String nombre,
      @RequestParam(required = false) String tipo,
      @RequestParam(required = false) String subTipo,
      @RequestParam(required = false) String medida,
      @RequestParam(required = false) String modelo,
      @RequestParam(required = false) String color,
      @RequestParam(required = false) Boolean sinPrecio
    ) {
        List<Producto> productos = productoRepository.obtenerProductos(id, nombre, tipo, subTipo, medida, modelo, color, sinPrecio);
        if (productos.isEmpty()) return List.of();

        List<Long> ids = productos.stream().map(Producto::getId).toList();

        // Bulk-fetch precios & costos in one query each — avoids N+1.
        Map<Long, List<ProductoListDTO.PrecioMini>> preciosByProducto = productoPrecioRepository
            .findActiveByProductoIdIn(ids).stream()
            .collect(Collectors.groupingBy(
                ProductoPrecio::getProductoId,
                Collectors.mapping(
                    pp -> new ProductoListDTO.PrecioMini(pp.getPrecio(), pp.getDescripcion(), pp.getCantidadRequerida()),
                    Collectors.toList())));

        Map<Long, List<ProductoListDTO.CostoMini>> costosByProducto = productoCostoRepository
            .findActiveByProductoIdIn(ids).stream()
            .collect(Collectors.groupingBy(
                ProductoCosto::getProductoId,
                Collectors.mapping(
                    pc -> new ProductoListDTO.CostoMini(pc.getTipoCosto(), pc.getCosto()),
                    Collectors.toList())));

        return productos.stream()
            .map(p -> new ProductoListDTO(
                p,
                preciosByProducto.getOrDefault(p.getId(), Collections.emptyList()),
                costosByProducto.getOrDefault(p.getId(), Collections.emptyList())))
            .toList();
    }

    @GetMapping("/config")
    public List<ProductoConfigDTO> getConfiguracionesValidas(
        @RequestParam(required = false) String tipo,
        @RequestParam(required = false) String subTipo,
        @RequestParam(required = false) String medida,
        @RequestParam(required = false) String modelo,
        @RequestParam(required = false) String color) {

        return productoRepository.obtenerConfiguracionesValidas(tipo, subTipo, medida, modelo, color);
    }

    @GetMapping("/{id}")
    public Producto getById(@PathVariable Long id) {
        return productoRepository.findById(id).orElseThrow();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Producto create(
            @RequestPart("producto") Producto product,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        return productoService.create(product, imagen);
    }

    @PostMapping(value = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProductoBulkResponse createBulk(@RequestBody ProductoBulkRequest request) {
        return productoService.createBulk(request);
    }

    @PostMapping(value = "/bulk-pricing", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProductoBulkPricingResponse applyBulkPricing(@RequestBody ProductoBulkPricingRequest request) {
        return productoService.applyBulkPricing(request);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Producto update(
            @PathVariable Long id,
            @RequestPart("producto") Producto product,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        product.setId(id);
        return productoService.update(product, imagen);
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
      return productoPrecioRepository.getAllProductoPrecios(id);
    }

    //TODO: Remover este, creo que ya no se ocupa
    @GetMapping("/search")
    public List<Producto> search(@RequestParam("q") String q, // @PathVariable("q") String q,
                                 @RequestParam(value = "limit", defaultValue = "20") int limit) {
      if (q == null || q.trim().isEmpty()) return List.of();
      String term = q.trim();
      List<Producto> results = productoRepository.searchByTerm(term.toLowerCase());
      return results.stream().limit(limit).toList();
    }

}

