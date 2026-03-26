package com.github.kraudy.InventoryBackend.scheduler;

import com.github.kraudy.InventoryBackend.service.OrdenCalendarioCarryOverService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrdenCalendarioScheduler {

    private final OrdenCalendarioCarryOverService carryOverService;

    // Runs every day at 6:00 AM (change time as you like)
    @Scheduled(cron = "0 0 6 * * ?")   // second, minute, hour, day, month, weekday
    public void runDailyCarryOver() {
        carryOverService.carryOverUnfinishedOrders();
    }

    // Also runs automatically when the server starts (in case it was shut down at night)
    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        System.out.println("🚀 Application started → running daily carry-over check...");
        carryOverService.carryOverUnfinishedOrders();
    }
}