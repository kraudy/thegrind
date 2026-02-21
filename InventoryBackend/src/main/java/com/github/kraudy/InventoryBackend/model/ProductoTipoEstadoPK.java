package com.github.kraudy.InventoryBackend.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProductoTipoEstadoPK implements Serializable {
  private String tipo;

  private String estado;
}
