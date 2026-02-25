package com.github.kraudy.InventoryBackend.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrdenSeguimientoHistoricoPK implements Serializable {
  private Long idOrden;

  private Long idOrdenDetalle;

  private String estado;
}