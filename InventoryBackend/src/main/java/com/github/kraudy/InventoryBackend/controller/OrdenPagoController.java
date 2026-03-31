package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.dto.OrdenPagoDTO;
import com.github.kraudy.InventoryBackend.model.OrdenPago;
import com.github.kraudy.InventoryBackend.repository.OrdenPagoRepository;
import com.github.kraudy.InventoryBackend.service.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-pago")
@CrossOrigin(origins = "http://localhost:4200")
public class OrdenPagoController {

    @Autowired
    private OrdenPagoRepository ordenPagoRepository;

    @Autowired
    private NotificationService notificationService;

    // Register a new advance payment on an order
    @PostMapping("/{idOrden}")
    public OrdenPago registrarPago(@PathVariable Long idOrden, @RequestBody OrdenPago pago) {
        pago.setIdOrden(idOrden);
        pago.setEstado("Pendiente");   //TODO: Set this as DEFAULT

        OrdenPago saved = ordenPagoRepository.save(pago);

        notificationService.notifyOrdenesPagoChanged();   // Notifica

        return saved;
    }

    // Get all payments for a specific order
    @GetMapping("/por-orden/{idOrden}")
    public List<OrdenPago> getByOrden(@PathVariable Long idOrden) {
        return ordenPagoRepository.findByIdOrden(idOrden);
    }

    // Get all payments with optional search + estado filter
    @GetMapping("/pendientes")
    public List<OrdenPagoDTO> getPendientes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String estado) {
        return ordenPagoRepository.findAllWithFilter(search, estado);
    }

    // Approve a payment
    @PutMapping("/{id}/aprobar")
    public void aprobar(@PathVariable Long id) {
      String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
      
      ordenPagoRepository.aprobarPago(id, currentUser);

      notificationService.notifyOrdenesPagoChanged(); // Notifica
    }

    // Elimina el pago pendiente (rechazar)
    @PutMapping("/{id}/rechazar")
    public void rechazar(@PathVariable Long id) {
      String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
      
      ordenPagoRepository.rechazarPago(id, currentUser);

      notificationService.notifyOrdenesPagoChanged(); // Notifica
    }
}