package com.example.flowfit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de pago desde el frontend con MercadoPago Bricks
 * Recibe el token generado por Bricks y los datos necesarios para procesar el
 * pago
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    /**
     * Token de tarjeta generado por MercadoPago Bricks en el frontend
     */
    private String token;

    /**
     * Monto de la transacción en COP (pesos colombianos)
     */
    private Double transactionAmount;

    /**
     * Descripción del pago (ej: "Plan de entrenamiento: Plan Básico")
     */
    private String description;

    /**
     * Número de cuotas (installments)
     */
    private Integer installments;

    /**
     * ID del método de pago (ej: "visa", "master")
     */
    private String paymentMethodId;

    /**
     * ID del emisor de la tarjeta (issuer)
     */
    private String issuer;

    /**
     * ID de la negociación/contratación en FlowFit
     */
    private Long negociacionId;

    /**
     * Datos del pagador
     */
    private PayerDTO payer;

    /**
     * DTO anidado para los datos del pagador
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayerDTO {
        private String email;
        private IdentificationDTO identification;
    }

    /**
     * DTO anidado para identificación del pagador
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdentificationDTO {
        private String type; // CC, CE, NIT, etc.
        private String number;
    }
}
