package com.example.flowfit.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    
    /**
     * Crea una preferencia de pago en MercadoPago
     * 
     * @param contratacionId ID de la contratación
     * @param monto Monto a cobrar
     * @param titulo Título del producto/servicio
     * @param descripcion Descripción del producto/servicio
     * @return URL de inicio de pago (init_point)
     */
    public String crearPreferenciaPago(Long contratacionId, BigDecimal monto, String titulo, String descripcion) {
        try {
            // Configurar el access token de MercadoPago
            MercadoPagoConfig.setAccessToken(accessToken);
            
            log.info("Creando preferencia de pago - Contratación ID: {}, Monto: {}", contratacionId, monto);
            
            // Crear el item de la preferencia
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(contratacionId.toString())
                    .title(titulo)
                    .description(descripcion)
                    .quantity(1)
                    .currencyId("COP") // Peso colombiano
                    .unitPrice(monto)
                    .build();
            
            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(item);
            
            // Configurar URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(appUrl + "/pagos/success?contratacion_id=" + contratacionId)
                    .failure(appUrl + "/pagos/failure?contratacion_id=" + contratacionId)
                    .pending(appUrl + "/pagos/pending?contratacion_id=" + contratacionId)
                    .build();
            
            // Configurar métodos de pago
            PreferencePaymentMethodsRequest paymentMethods = PreferencePaymentMethodsRequest.builder()
                    .installments(12) // Permitir hasta 12 cuotas
                    .build();
            
            // Crear la preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .paymentMethods(paymentMethods)
                    .externalReference(contratacionId.toString()) // Referencia externa para tracking
                    .statementDescriptor("FLOWFIT") // Descripción que aparece en el extracto bancario
                    .notificationUrl(appUrl + "/api/webhooks/mercadopago") // URL para notificaciones IPN
                    .build();
            
            // Crear el cliente y enviar la preferencia
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);
            
            log.info("Preferencia creada exitosamente - ID: {}, Init Point: {}", 
                    preference.getId(), preference.getInitPoint());
            
            return preference.getInitPoint();
            
        } catch (MPException e) {
            log.error("Error de MercadoPago al crear preferencia: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getMessage(), e);
        } catch (MPApiException e) {
            log.error("Error de API de MercadoPago - Status: {}, Contenido: {}", 
                    e.getStatusCode(), e.getApiResponse().getContent(), e);
            throw new RuntimeException("Error en la API de MercadoPago: " + e.getApiResponse().getContent(), e);
        } catch (Exception e) {
            log.error("Error inesperado al crear preferencia de pago", e);
            throw new RuntimeException("Error inesperado al procesar el pago: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene la public key para uso en el frontend
     * 
     * @return Public key de MercadoPago
     */
    public String getPublicKey() {
        return publicKey;
    }
    
    /**
     * Verifica si está en modo sandbox (pruebas)
     * 
     * @return true si está en modo sandbox
     */
    public boolean isSandboxMode() {
        return "sandbox".equalsIgnoreCase(mode);
    }
    
    /**
     * Crea una preferencia de pago simplificada para planes de entrenamiento
     * 
     * @param contratacionId ID de la contratación
     * @param nombrePlan Nombre del plan
     * @param nombreEntrenador Nombre del entrenador
     * @param precioFinal Precio final acordado
     * @return URL de inicio de pago
     */
    public String crearPreferenciaPlan(Long contratacionId, String nombrePlan, 
                                       String nombreEntrenador, BigDecimal precioFinal) {
        String titulo = "Plan de Entrenamiento: " + nombrePlan;
        String descripcion = "Entrenador: " + nombreEntrenador + " | Plan personalizado de entrenamiento";
        
        return crearPreferenciaPago(contratacionId, precioFinal, titulo, descripcion);
    }
}
