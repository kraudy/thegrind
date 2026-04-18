package com.github.kraudy.InventoryBackend.service;

import com.github.kraudy.InventoryBackend.model.Producto;
import com.github.kraudy.InventoryBackend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CurrentUserService currentUserService;

    @Value("${app.upload.dir:/app/images}")
    private String uploadDir;

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/jpg");

    private Path getUploadPath() {
        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            try { Files.createDirectories(path); } 
            catch (IOException e) { throw new RuntimeException("No se pudo crear directorio de imágenes", e); }
        }
        return path;
    }


    @Transactional
    public Producto create(Producto producto, MultipartFile imagen) {
      // Check duplicate on create
      //TODO: Creo que este lo voy a tener que quitar.
      if (productoRepository.existeProducto(
            producto.getTipoProducto(),
            producto.getSubTipoProducto(),
            producto.getMedidaProducto(),
            producto.getModeloProducto(),
          producto.getColorProducto())) {

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Ya existe un producto con el mismo tipo, subtipo, medida, modelo y color");
      }

      String currentUser = currentUserService.getCurrentUser();

      producto.setNombre(toTitleCase(producto.getNombre()));
      producto.setDescripcion(toTitleCase(producto.getDescripcion()));
      producto.setUsuarioCreacion(currentUser);
      producto.setUsuarioModificacion(currentUser);

      Producto saved = productoRepository.save(producto);

      // 2. Procesar imagen (si viene)
      if (imagen != null && !imagen.isEmpty()) {
          saved.setImagen(processAndSaveImage(saved.getId(), imagen));
          saved = productoRepository.save(saved);
      }

      return saved;

    }

    @Transactional
    public Producto update(Producto producto, MultipartFile imagen) {
        // Check duplicate on update (allow same product to keep its own values)
        //TODO: Creo que este lo voy a tener que quitar.
        if (productoRepository.existeProductoDiferenteId(
              producto.getTipoProducto(),
              producto.getSubTipoProducto(),
              producto.getMedidaProducto(),
              producto.getModeloProducto(),
              producto.getColorProducto(),
              producto.getId())) {

          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                  "Ya existe otro producto con el mismo tipo, subtipo, medida, modelo y color");
        }

        producto.setNombre(toTitleCase(producto.getNombre()));
        producto.setDescripcion(toTitleCase(producto.getDescripcion()));

        Producto existente = productoRepository.findById(producto.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        String currentUser = currentUserService.getCurrentUser();

        // Solo copiamos los campos que queremos actualizar
        existente.setNombre(producto.getNombre());
        existente.setDescripcion(producto.getDescripcion());
        existente.setTipoProducto(producto.getTipoProducto());
        existente.setSubTipoProducto(producto.getSubTipoProducto());
        existente.setMedidaProducto(producto.getMedidaProducto());
        existente.setModeloProducto(producto.getModeloProducto());
        existente.setColorProducto(producto.getColorProducto());
        existente.setActivo(producto.isActivo());
        existente.setUsuarioModificacion(currentUser);

        // 3. Imagen
        if (imagen != null && !imagen.isEmpty()) {
            // Borrar imagen anterior si existía
            if (existente.getImagen() != null) {
                deleteOldImage(existente.getImagen());
            }
            existente.setImagen(processAndSaveImage(existente.getId(), imagen));
        }

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

    private String processAndSaveImage(Long id, MultipartFile file) {
      if (!ALLOWED_TYPES.contains(file.getContentType())) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten PNG y JPG/JPEG");
      }

      String extension = file.getOriginalFilename().toLowerCase().endsWith(".png") ? "png" : "jpg";
      String filename = "producto-" + id + "." + extension;
      Path target = getUploadPath().resolve(filename);

      try {
          Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
          throw new RuntimeException("Error al guardar imagen", e);
      }

      return filename;   // ← guardamos solo el nombre (más limpio)
    }

    private void deleteOldImage(String oldPath) {
      if (oldPath == null) return;
      try {
          Files.deleteIfExists(getUploadPath().resolve(oldPath));
      } catch (IOException ignored) {}
    }

    // Helper para obtener ruta completa (útil para frontend)
    public String getFullImageUrl(String filename) {
        return filename != null ? "/images/" + filename : null;
    }
}