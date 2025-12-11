package com.example.flowfit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la configuración de MercadoPago que se envía al frontend
 * Contiene la public key y configuración necesaria para inicializar Bricks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoConfigDTO {

    /**
     * Public Key de MercadoPago (TEST o PROD)
     */
    private String publicKey;

    /**
     * Locale para el idioma (ej: "es-CO")
     */
    private String locale;

    /**
     * Indica si está en modo TEST
     */
    private Boolean testMode;

    /**
     * URL base de la aplicación
     */
    private String appUrl;
}
