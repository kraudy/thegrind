package com.github.kraudy.InventoryBackend.service;

import com.github.kraudy.InventoryBackend.model.OrdenCalendario;
import com.github.kraudy.InventoryBackend.model.OrdenCalendarioHistorico;
import com.github.kraudy.InventoryBackend.repository.OrdenCalendarioRepository;
import com.github.kraudy.InventoryBackend.repository.OrdenCalendarioHistoricoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdenCalendarioCarryOverService {

    private final OrdenCalendarioRepository ordenCalendarioRepository;
    private final OrdenCalendarioHistoricoRepository historicoRepository;

    @Transactional
    public void carryOverUnfinishedOrders() {
      LocalDate today = LocalDate.now();

      // 1. Find all orders scheduled before today that are still in 'Repartida'.
      //    Orders already 'Listo'/'Entregado'/'Facturado' don't need carry-over because
      //    the work is done.
      List<OrdenCalendario> overdue = ordenCalendarioRepository
              .findOverdueRepartidas(today);

      if (overdue.isEmpty()) {
        System.out.println("✅ Carry-over: No overdue orders found.");
        return;
      }

      System.out.println("🔄 Carry-over started: " + overdue.size() + " orders to move to today.");

      for (OrdenCalendario oc : overdue) {
        // 2. Save to history
        OrdenCalendarioHistorico hist = new OrdenCalendarioHistorico();
        hist.setIdOrden(oc.getIdOrden());
        hist.setFechaTrabajoOriginal(oc.getFechaTrabajo());
        hist.setFechaOriginal(oc.getFecha());
        hist.setFechaArchivado(LocalDateTime.now());
        historicoRepository.save(hist);

        // 3. Update original to TODAY
        oc.setFecha(today);
        oc.setFechaTrabajo(LocalDateTime.now()); // or today at 08:00 if you prefer

        // 4. Bump prioridad: Normal -> Alta, Alta -> Urgente, Urgente stays
        oc.setPrioridad(bumpPrioridad(oc.getPrioridad()));
      }

      ordenCalendarioRepository.saveAll(overdue);

      System.out.println("✅ Carry-over completed: " + overdue.size() + " orders moved to today.");
  }

  private String bumpPrioridad(String current) {
    if (current == null || "Normal".equals(current)) return "Alta";
    if ("Alta".equals(current)) return "Urgente";
    return current; // Urgente or unknown stays as-is
  }
}