package com.github.kraudy.InventoryBackend.dto;

import java.time.LocalDateTime;

public record UsuarioDTO(
  String usuario,
  LocalDateTime fechaCreacion,
  String usuarioCreacion,
  LocalDateTime fechaModificacion,
  String usuarioModificacion,
  boolean activo
) {}