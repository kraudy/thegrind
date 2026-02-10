package com.github.kraudy.InventoryBackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrdenSeguimientoPK implements Serializable {
  private Long idOrden;

  private Long idOrdenDetalle;

  private Long idProducto;

  private String estado;
}