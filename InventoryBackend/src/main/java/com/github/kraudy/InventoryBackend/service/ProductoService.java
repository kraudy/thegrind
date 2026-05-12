package com.github.kraudy.InventoryBackend.service;

import com.github.kraudy.InventoryBackend.dto.ProductoBulkRequest;
import com.github.kraudy.InventoryBackend.dto.ProductoBulkResponse;
import com.github.kraudy.InventoryBackend.dto.ProductoConfigDTO;
import com.github.kraudy.InventoryBackend.model.Producto;
import com.github.kraudy.InventoryBackend.model.ProductoCosto;
import com.github.kraudy.InventoryBackend.model.ProductoPrecio;
import com.github.kraudy.InventoryBackend.repository.ProductoCostoRepository;
import com.github.kraudy.InventoryBackend.repository.ProductoPrecioRepository;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoPrecioRepository productoPrecioRepository;
    private final ProductoCostoRepository productoCostoRepository;
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

    /**
     * Bulk-create productos for every (medida x color) combination provided.
     *
     * For each combination:
     *   - Skip silently if the combo doesn't exist in producto_config (invalid_combination)
     *   - Skip silently if a producto with that exact 5-tuple already exists (already_exists)
     *   - Otherwise insert the Producto plus all shared precios and costos
     *
     * The whole operation runs in a single transaction: if any insert blows up
     * (e.g. DB trigger rejects it), the entire batch is rolled back.
     */
    @Transactional
    public ProductoBulkResponse createBulk(ProductoBulkRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request vacío");
        }
        if (request.getTipoProducto() == null || request.getTipoProducto().isBlank()
                || request.getSubTipoProducto() == null || request.getSubTipoProducto().isBlank()
                || request.getModeloProducto() == null || request.getModeloProducto().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "tipo, subTipo y modelo son requeridos");
        }
        if (request.getMedidas() == null || request.getMedidas().isEmpty()
                || request.getColores() == null || request.getColores().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe seleccionar al menos una medida y un color");
        }
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre es requerido");
        }

        // Pre-fetch all valid configs for the tipo/subTipo/modelo so we can validate
        // each (medida, color) pair locally instead of hitting the DB N times.
        List<ProductoConfigDTO> validConfigs = productoRepository.obtenerConfiguracionesValidas(
                request.getTipoProducto(),
                request.getSubTipoProducto(),
                null,
                request.getModeloProducto(),
                null
        );

        Set<String> validKeys = new HashSet<>();
        for (ProductoConfigDTO cfg : validConfigs) {
            validKeys.add(cfg.medida() + "|" + cfg.color());
        }

        String currentUser = currentUserService.getCurrentUser();
        String nombre = toTitleCase(request.getNombre());
        String descripcion = toTitleCase(request.getDescripcion());
        boolean activo = request.getActivo() == null ? true : request.getActivo();

        List<Producto> created = new ArrayList<>();
        List<ProductoBulkResponse.Skipped> skipped = new ArrayList<>();
        int totalRequested = 0;

        for (String medida : request.getMedidas()) {
            for (String color : request.getColores()) {
                totalRequested++;

                if (!validKeys.contains(medida + "|" + color)) {
                    skipped.add(new ProductoBulkResponse.Skipped(medida, color, "invalid_combination"));
                    continue;
                }

                if (productoRepository.existeProducto(
                        request.getTipoProducto(),
                        request.getSubTipoProducto(),
                        medida,
                        request.getModeloProducto(),
                        color)) {
                    skipped.add(new ProductoBulkResponse.Skipped(medida, color, "already_exists"));
                    continue;
                }

                Producto p = new Producto();
                p.setTipoProducto(request.getTipoProducto());
                p.setSubTipoProducto(request.getSubTipoProducto());
                p.setMedidaProducto(medida);
                p.setModeloProducto(request.getModeloProducto());
                p.setColorProducto(color);
                p.setNombre(color != null && !color.equalsIgnoreCase("Ninguno")
                        ? nombre + " " + toTitleCase(color)
                        : nombre);
                p.setDescripcion(descripcion == null ? "" : descripcion);
                p.setActivo(activo);
                p.setUsuarioCreacion(currentUser);
                p.setUsuarioModificacion(currentUser);

                Producto saved = productoRepository.save(p);

                // Precios (shared across all generated productos)
                if (request.getPrecios() != null) {
                    for (ProductoBulkRequest.PrecioItem pi : request.getPrecios()) {
                        if (pi.getPrecio() == null) continue;
                        ProductoPrecio pp = new ProductoPrecio();
                        pp.setProductoId(saved.getId());
                        pp.setPrecio(pi.getPrecio());
                        pp.setDescripcion(pi.getDescripcion() == null ? "" : pi.getDescripcion());
                        pp.setCantidadRequerida(pi.getCantidadRequerida() == null ? 0 : pi.getCantidadRequerida());
                        pp.setActivo(true);
                        pp.setUsuarioCreacion(currentUser);
                        pp.setUsuarioModificacion(currentUser);
                        productoPrecioRepository.save(pp);
                    }
                }

                // Costos (shared across all generated productos)
                if (request.getCostos() != null) {
                    for (ProductoBulkRequest.CostoItem ci : request.getCostos()) {
                        if (ci.getTipoCosto() == null || ci.getTipoCosto().isBlank()) continue;
                        if (ci.getCosto() == null) continue;
                        ProductoCosto pc = new ProductoCosto();
                        pc.setProductoId(saved.getId());
                        pc.setTipoCosto(ci.getTipoCosto());
                        pc.setCosto(ci.getCosto());
                        pc.setDescripcion(ci.getDescripcion() == null ? "" : ci.getDescripcion());
                        pc.setCantidadRequerida(ci.getCantidadRequerida() == null ? 0 : ci.getCantidadRequerida());
                        pc.setActivo(true);
                        pc.setUsuarioCreacion(currentUser);
                        pc.setUsuarioModificacion(currentUser);
                        productoCostoRepository.save(pc);
                    }
                }

                created.add(saved);
            }
        }

        return new ProductoBulkResponse(created, skipped, totalRequested, created.size());
    }
}