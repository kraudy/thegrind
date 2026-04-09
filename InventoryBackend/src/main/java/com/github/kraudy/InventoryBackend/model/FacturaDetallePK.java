package com.github.kraudy.InventoryBackend.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FacturaDetallePK implements Serializable {
  private Long idFactura;

  private Long idDetalle;
}