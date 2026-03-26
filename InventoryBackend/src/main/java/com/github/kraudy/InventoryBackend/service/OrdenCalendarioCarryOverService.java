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

        // 1. Find all orders scheduled before today
        List<OrdenCalendario> overdue = ordenCalendarioRepository
                .findByFechaBefore(today);

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
            // Optional: you can also reset estado if needed
        }

        ordenCalendarioRepository.saveAll(overdue);

        System.out.println("✅ Carry-over completed: " + overdue.size() + " orders moved to today.");
    }
}