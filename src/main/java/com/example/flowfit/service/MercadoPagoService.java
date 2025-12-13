package com.example.flowfit.service;

import com.example.flowfit.dto.PaymentRequestDTO;
import com.example.flowfit.dto.PaymentResponseDTO;
import com.example.flowfit.model.ContratacionEntrenador;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para MercadoPago con Checkout Bricks
 * Procesa pagos usando la Payment API REST (sin SDK)
 */
@Slf4j
@Service
public class MercadoPagoService {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.public-key}")
    private String publicKey;

    @Value("${app.url}")
    private String appUrl;

    @Value("${mercadopago.mode}")
    private String mode;

    private static final String MP_API_URL = "https://api.mercadopago.com/v1/payments";

    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();

    /**
     * Obtiene la clave pública para inicializar Bricks en el frontend
     * 
     * @return Clave pública de MercadoPago
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Obtiene el token de acceso (solo si se necesita para alguna validación)
     * 
     * @return Token de acceso de MercadoPago
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Verifica si está en modo test
     * 
     * @return true si está en modo test
     */
    public boolean isTestMode() {
        return "test".equalsIgnoreCase(mode);
    }

    /**
     * Obtiene la URL base de la aplicación
     * 
     * @return URL base
     */
    public String getAppUrl() {
        return appUrl;
    }

    /**
     * Procesa un pago usando el token generado por Bricks
     * Llama a la Payment API de MercadoPago usando REST
     * 
     * @param paymentRequest Datos del pago desde el frontend
     * @param contratacion   Contratación asociada
     * @return PaymentResponseDTO con el resultado
     */
    public PaymentResponseDTO procesarPago(PaymentRequestDTO paymentRequest, ContratacionEntrenador contratacion) {
        try {
            log.info("💳 Enviando pago a MercadoPago API...");

            // Construir el payload para la Payment API
            Map<String, Object> paymentPayload = new HashMap<>();
            paymentPayload.put("token", paymentRequest.getToken());
            paymentPayload.put("transaction_amount", paymentRequest.getTransactionAmount());
            paymentPayload.put("description", paymentRequest.getDescription());
            paymentPayload.put("installments",
                    paymentRequest.getInstallments() != null ? paymentRequest.getInstallments() : 1);
            paymentPayload.put("payment_method_id", paymentRequest.getPaymentMethodId());

            // Solo agregar issuer_id si no es null
            if (paymentRequest.getIssuer() != null && !paymentRequest.getIssuer().isEmpty()) {
                paymentPayload.put("issuer_id", paymentRequest.getIssuer());
            }

            paymentPayload.put("external_reference", "NEG-" + contratacion.getId());

            // Información del pagador
            Map<String, Object> payer = new HashMap<>();
            payer.put("email", paymentRequest.getPayer().getEmail());

            Map<String, Object> identification = new HashMap<>();
            identification.put("type", paymentRequest.getPayer().getIdentification().getType());
            identification.put("number", paymentRequest.getPayer().getIdentification().getNumber());
            payer.put("identification", identification);

            paymentPayload.put("payer", payer);

            // Configurar headers con Access Token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("X-Idempotency-Key", "NEG-" + contratacion.getId() + "-" + System.currentTimeMillis());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(paymentPayload, headers);

            // Llamar a la API de MercadoPago
            log.info("🌐 POST {} - Amount: {}", MP_API_URL, paymentRequest.getTransactionAmount());
            ResponseEntity<String> response = restTemplate.exchange(
                    MP_API_URL,
                    HttpMethod.POST,
                    request,
                    String.class);

            // Parsear respuesta
            JsonObject jsonResponse = gson.fromJson(response.getBody(), JsonObject.class);

            log.info("✅ Respuesta de MercadoPago - Status: {} ({})",
                    jsonResponse.get("status").getAsString(),
                    jsonResponse.get("status_detail").getAsString());

            // Construir DTO de respuesta
            return PaymentResponseDTO.builder()
                    .id(jsonResponse.get("id").getAsLong())
                    .status(jsonResponse.get("status").getAsString())
                    .statusDetail(jsonResponse.get("status_detail").getAsString())
                    .transactionAmount(jsonResponse.get("transaction_amount").getAsDouble())
                    .currencyId(jsonResponse.get("currency_id").getAsString())
                    .paymentMethodId(jsonResponse.get("payment_method_id").getAsString())
                    .paymentTypeId(jsonResponse.get("payment_type_id").getAsString())
                    .installments(jsonResponse.get("installments").getAsInt())
                    .description(jsonResponse.get("description").getAsString())
                    .payerEmail(paymentRequest.getPayer().getEmail())
                    .success(true)
                    .build();

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("❌ Error HTTP de MercadoPago: {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());

            // Intentar extraer mensaje de error de MercadoPago
            String errorMessage = "Error al procesar el pago";
            String errorDetail = "";

            try {
                JsonObject errorJson = gson.fromJson(e.getResponseBodyAsString(), JsonObject.class);
                if (errorJson.has("message")) {
                    errorMessage = errorJson.get("message").getAsString();
                }
                if (errorJson.has("cause")) {
                    JsonObject cause = errorJson.getAsJsonArray("cause").get(0).getAsJsonObject();
                    errorDetail = cause.get("description").getAsString();
                }
            } catch (Exception parseError) {
                log.warn("No se pudo parsear error de MercadoPago");
            }

            return PaymentResponseDTO.builder()
                    .success(false)
                    .error(errorMessage + (errorDetail.isEmpty() ? "" : ": " + errorDetail))
                    .status("rejected")
                    .statusDetail("error_processing")
                    .build();

        } catch (Exception e) {
            log.error("❌ Error inesperado al procesar pago: {}", e.getMessage(), e);

            return PaymentResponseDTO.builder()
                    .success(false)
                    .error("Error inesperado al procesar el pago: " + e.getMessage())
                    .status("rejected")
                    .statusDetail("error_processing")
                    .build();
        }
    }
}
