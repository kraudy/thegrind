package com.github.kraudy.InventoryBackend.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Call this from any controller when something important changes
    public void notifyOrdenesSeguimientoChanged() {
        messagingTemplate.convertAndSend("/topic/ordenes-seguimiento", "REFRESH");
    }

    /* Actualiza el calendario */
    public void notifyCalendarioChanged() {
        messagingTemplate.convertAndSend("/topic/ordenes-calendario", "REFRESH");
    }

    public void notifyOrdenesTrabajoChanged() {
        messagingTemplate.convertAndSend("/topic/ordenes-trabajo", "REFRESH");
    }

    /* Notifica de nuevo costo agregado a reparacion o pegado para que se actualice el componente correspondiente */
    public void notifyOrdenesCostoChanged() {
        messagingTemplate.convertAndSend("/topic/ordenes-costo", "REFRESH");
    }

    public void notifyOrdenesPagoChanged() {
        messagingTemplate.convertAndSend("/topic/ordenes-pago", "REFRESH");
    }

    public void notifyFacturasChanged() {
        messagingTemplate.convertAndSend("/topic/facturas", "REFRESH");
    }
}