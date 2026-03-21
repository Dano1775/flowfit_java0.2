package com.example.flowfit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Configuración web para servir archivos estáticos adicionales
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ruta absoluta al directorio de imágenes de ejercicios
        String exerciseUploadPath = Paths.get(System.getProperty("user.dir"),
                "src", "main", "resources", "static", "ejercicio_image_uploads")
                .toAbsolutePath()
                .toUri()
                .toString();

        // Registrar handler para servir imágenes de ejercicios
        registry.addResourceHandler("/ejercicio_image_uploads/**")
                .addResourceLocations(exerciseUploadPath);

        // Ruta absoluta al directorio de archivos del chat
        String chatUploadPath = Paths.get(System.getProperty("user.dir"),
                "src", "main", "resources", "static", "chat_uploads")
                .toAbsolutePath()
                .toUri()
                .toString();

        // Registrar handler para servir archivos del chat
        registry.addResourceHandler("/chat_uploads/**")
                .addResourceLocations(chatUploadPath);

        System.out.println(" Configuración de archivos:");
        System.out.println("    Ejercicios: " + exerciseUploadPath);
        System.out.println("    Chat: " + chatUploadPath);
    }
}
