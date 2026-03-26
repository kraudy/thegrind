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

    public void notifyCalendarioChanged() {
        messagingTemplate.convertAndSend("/topic/ordenes-calendario", "REFRESH");
    }

    public void notifyOrdenesTrabajoChanged() {
        messagingTemplate.convertAndSend("/topic/ordenes-trabajo", "REFRESH");
    }

    // You can add more specific ones later (e.g. by order ID)
}