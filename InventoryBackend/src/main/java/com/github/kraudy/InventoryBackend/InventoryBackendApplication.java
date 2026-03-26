package com.github.kraudy.InventoryBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling          // Habilita la ejecución de tareas programadas (cron jobs) para el backend
public class InventoryBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryBackendApplication.class, args);
	}

}
