package com.github.kraudy.InventoryBackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.upload.dir:/app/images}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
      registry.addResourceHandler("/images/**")
              .addResourceLocations("file:" + uploadDir + "/")
              .setCachePeriod(3600) // 1 hour cache (optional but nice)
              ;
  }
  
  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    // Handles ANY client-side route (1, 2, 3 or more segments)
    //registry.addViewController("/{path:[^\\.]*}/**")
    registry.addViewController("/**/{path:[^\\.]*}")
            .setViewName("forward:/index.html");
  }
}