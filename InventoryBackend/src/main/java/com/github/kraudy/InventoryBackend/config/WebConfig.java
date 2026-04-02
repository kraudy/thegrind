package com.github.kraudy.InventoryBackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
      // Handles ANY client-side route (1, 2, 3 or more segments)
      registry.addViewController("/{path:[^\\.]*}/**")
              .setViewName("forward:/index.html");
    }
}