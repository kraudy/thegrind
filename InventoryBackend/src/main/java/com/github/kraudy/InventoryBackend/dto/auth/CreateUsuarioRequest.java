package com.github.kraudy.InventoryBackend.dto.auth;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateUsuarioRequest(
  String usuario,
  String password,
  boolean activo,
  List<String> roles
) {}

