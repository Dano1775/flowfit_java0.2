package com.example.flowfit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de pago al frontend
 * Contiene el resultado del procesamiento con MercadoPago
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    /**
     * ID del pago en MercadoPago
     */
    private Long id;

    /**
     * Estado del pago: approved, pending, rejected, etc.
     */
    private String status;

    /**
     * Detalle del estado (ej: "accredited", "pending_contingency",
     * "cc_rejected_bad_filled_card_number")
     */
    private String statusDetail;

    /**
     * Monto de la transacción
     */
    private Double transactionAmount;

    /**
     * Moneda (COP)
     */
    private String currencyId;

    /**
     * Método de pago utilizado
     */
    private String paymentMethodId;

    /**
     * Tipo de método de pago (credit_card, debit_card, etc.)
     */
    private String paymentTypeId;

    /**
     * Número de cuotas
     */
    private Integer installments;

    /**
     * Descripción del pago
     */
    private String description;

    /**
     * Email del pagador
     */
    private String payerEmail;

    /**
     * Indica si la operación fue exitosa
     */
    private Boolean success;

    /**
     * Mensaje adicional
     */
    private String message;

    /**
     * Mensaje de error (si aplica)
     */
    private String error;

    /**
     * URL de redirección (opcional)
     */
    private String redirectUrl;

    /**
     * ID de la negociación asociada en FlowFit
     */
    private Long negociacionId;
}
