package com.github.kraudy.InventoryBackend.service;

import com.github.kraudy.InventoryBackend.model.Producto;
import com.github.kraudy.InventoryBackend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Transactional
    public Producto create(Producto producto) {
      // Check duplicate on create
      if (productoRepository.existeProducto(
            producto.getTipoProducto(),
            producto.getSubTipoProducto(),
            producto.getMedidaProducto(),
            producto.getModeloProducto())) {

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Ya existe un producto con el mismo tipo, subtipo, medida y modelo");
      }

      producto.setNombre(toTitleCase(producto.getNombre()));
      producto.setDescripcion(toTitleCase(producto.getDescripcion()));

      return productoRepository.save(producto);
    }

    @Transactional
    public Producto update(Producto producto) {
        // Check duplicate on update (allow same product to keep its own values)
        if (productoRepository.existeProductoDiferenteId(
              producto.getTipoProducto(),
              producto.getSubTipoProducto(),
              producto.getMedidaProducto(),
              producto.getModeloProducto(),
              producto.getId())) {

          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                  "Ya existe otro producto con el mismo tipo, subtipo, medida y modelo");
        }

        producto.setNombre(toTitleCase(producto.getNombre()));
        producto.setDescripcion(toTitleCase(producto.getDescripcion()));

        Producto existente = productoRepository.findById(producto.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        // Solo copiamos los campos que queremos actualizar
        existente.setNombre(producto.getNombre());
        existente.setDescripcion(producto.getDescripcion());
        existente.setTipoProducto(producto.getTipoProducto());
        existente.setSubTipoProducto(producto.getSubTipoProducto());
        existente.setMedidaProducto(producto.getMedidaProducto());
        existente.setModeloProducto(producto.getModeloProducto());
        existente.setActivo(producto.isActivo());

        return productoRepository.save(existente);
    }

    private String toTitleCase(String input) {
      if (input == null || input.trim().isEmpty()) {
          return input;
      }
      return java.util.Arrays.stream(input.trim().split("\\s+"))
              .map(word -> word.isEmpty() ? word :
                      word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
              .collect(java.util.stream.Collectors.joining(" "));
    }
}