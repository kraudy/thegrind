package com.github.kraudy.InventoryBackend.dto.auth;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record UsuarioAdminDTO(
  String usuario,
  boolean activo,
  String[] roles
) {}
