package com.example.flowfit.controller;

import com.example.flowfit.model.*;
import com.example.flowfit.service.*;
import com.example.flowfit.repository.*;
import com.example.flowfit.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para procesar pagos directos con MercadoPago Checkout Bricks
 */
@Slf4j
@Controller
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private ContratacionEntrenadorRepository contratacionRepo;

    @Autowired
    private PagoContratacionRepository pagoRepo;

    @Autowired
    private ConversacionRepository conversacionRepo;

    @Autowired
    private MensajeRepository mensajeRepo;

    @Autowired
    private AsignacionEntrenadorRepository asignacionRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * MODO DEMO: Simular pagos sin MercadoPago
     * true = Simula pagos (para presentaciones sin SSL)
     * false = Usa MercadoPago real con Checkout Bricks
     */
    private static final boolean MODO_DEMO = false;

    /**
     * Procesar pago con MercadoPago Bricks (recibe token y procesa en backend)
     */
    @PostMapping("/procesar")
    @ResponseBody
    public ResponseEntity<PaymentResponseDTO> procesarPago(
            @RequestBody PaymentRequestDTO paymentRequest,
            HttpSession session) {

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(PaymentResponseDTO.builder()
                                .success(false)
                                .message("Sesión no válida")
                                .build());
            }

            log.info("💳 Procesando pago con Bricks - Token: {}, Amount: {}, NegociacionId: {}",
                    paymentRequest.getToken().substring(0, 10) + "...",
                    paymentRequest.getTransactionAmount(),
                    paymentRequest.getNegociacionId());

            // Validar contratación
            ContratacionEntrenador contratacion = contratacionRepo.findById(paymentRequest.getNegociacionId())
                    .orElseThrow(() -> new RuntimeException("Contratación no encontrada"));

            if (!contratacion.getUsuarioId().equals(usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(PaymentResponseDTO.builder()
                                .success(false)
                                .message("No tienes permiso para pagar esta contratación")
                                .build());
            }

            // Procesar pago con MercadoPago usando el token de Bricks
            PaymentResponseDTO resultadoPago = mercadoPagoService.procesarPago(paymentRequest, contratacion);

            log.info("✅ Pago procesado - Payment ID: {}, Status: {}",
                    resultadoPago.getId(), resultadoPago.getStatus());

            // Crear registro de pago en la base de datos
            PagoContratacion pago = new PagoContratacion();
            pago.setContratacionId(contratacion.getId());
            pago.setUsuarioId(usuario.getId());
            pago.setMonto(BigDecimal.valueOf(resultadoPago.getTransactionAmount()));
            pago.setMoneda(resultadoPago.getCurrencyId() != null ? resultadoPago.getCurrencyId() : "COP");
            pago.setMpPaymentId(resultadoPago.getId().toString());
            pago.setMpStatus(resultadoPago.getStatus());
            pago.setMpStatusDetail(resultadoPago.getStatusDetail());
            pago.setMpPaymentMethod(resultadoPago.getPaymentMethodId());
            pago.setMpPaymentType(resultadoPago.getPaymentTypeId());
            pago.setMpExternalReference(contratacion.getId().toString());

            // Determinar estado del pago según respuesta de MercadoPago
            String mpStatus = resultadoPago.getStatus();
            switch (mpStatus) {
                case "approved":
                    pago.setEstadoPago(PagoContratacion.EstadoPago.APROBADO);
                    pago.setEstadoEscrow(PagoContratacion.EstadoEscrow.RETENIDO);
                    pago.setFechaPago(LocalDateTime.now());

                    // Calcular fecha límite para disputa (7 días después del fin del contrato)
                    LocalDateTime fechaFin = contratacion.getFechaInicio() != null
                            ? contratacion.getFechaInicio().plusDays(contratacion.getDuracionDiasAcordada())
                            : LocalDateTime.now().plusDays(contratacion.getDuracionDiasAcordada());
                    pago.setFechaLimiteDisputa(fechaFin.plusDays(7));

                    // Actualizar estado de contratación
                    contratacion.setEstado(ContratacionEntrenador.EstadoContratacion.ACTIVA);
                    contratacion.setFechaInicio(LocalDateTime.now());
                    contratacion.setFechaFin(LocalDateTime.now().plusDays(contratacion.getDuracionDiasAcordada()));

                    // Crear asignación automática de entrenador al usuario
                    crearAsignacionAutomatica(usuario, contratacion.getEntrenadorId());
                    break;

                case "pending":
                    pago.setEstadoPago(PagoContratacion.EstadoPago.PROCESANDO);
                    contratacion.setEstado(ContratacionEntrenador.EstadoContratacion.PAGO_PROCESANDO);
                    break;

                case "rejected":
                case "cancelled":
                    pago.setEstadoPago(PagoContratacion.EstadoPago.RECHAZADO);
                    break;

                default:
                    pago.setEstadoPago(PagoContratacion.EstadoPago.PENDIENTE);
            }

            pagoRepo.save(pago);
            contratacionRepo.save(contratacion);

            // Crear mensaje en el chat notificando el pago
            Conversacion conversacion = conversacionRepo
                    .findByUsuarioIdAndEntrenadorId(usuario.getId(), contratacion.getEntrenadorId())
                    .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));

            Mensaje mensaje = new Mensaje();
            mensaje.setConversacionId(conversacion.getId());
            mensaje.setRemitenteId(usuario.getId());
            mensaje.setTipoMensaje(Mensaje.TipoMensaje.SISTEMA);

            if ("approved".equals(mpStatus)) {
                mensaje.setContenido("¡Pago aprobado exitosamente!\n\n" +
                        "Monto: $" + String.format("%,.0f", resultadoPago.getTransactionAmount()) + " COP\n" +
                        "El dinero está retenido de forma segura hasta que ambos confirmen el servicio.\n" +
                        "Duración: " + contratacion.getDuracionDiasAcordada() + " días");
            } else if ("pending".equals(mpStatus)) {
                mensaje.setContenido("Pago en proceso\n\n" +
                        "Tu pago está siendo verificado. Te notificaremos cuando sea aprobado.");
            } else {
                mensaje.setContenido("Pago rechazado\n\n" +
                        "Hubo un problema al procesar tu pago. Por favor, intenta con otro método de pago.");
            }

            mensaje.setMetadata("{\"pagoId\": " + pago.getId() +
                    ", \"paymentId\": \"" + resultadoPago.getId() +
                    "\", \"status\": \"" + mpStatus + "\"}");
            Mensaje mensajeGuardado = mensajeRepo.save(mensaje);

            // 🔔 NOTIFICACIÓN WEBSOCKET EN TIEMPO REAL
            if ("approved".equals(mpStatus)) {
                Map<String, Object> notificacion = new HashMap<>();
                notificacion.put("tipo", "PAGO_APROBADO");
                notificacion.put("pagoId", pago.getId());
                notificacion.put("contratacionId", contratacion.getId());
                notificacion.put("monto", resultadoPago.getTransactionAmount());
                notificacion.put("duracionDias", contratacion.getDuracionDiasAcordada());
                notificacion.put("mensaje", mensajeGuardado.getContenido());
                notificacion.put("timestamp", LocalDateTime.now().toString());

                // Enviar al usuario
                messagingTemplate.convertAndSend(
                        "/topic/conversacion/" + conversacion.getId(),
                        notificacion);

                log.info("🔔 Notificación WebSocket enviada - Pago aprobado para conversación {}",
                        conversacion.getId());
            }

            // Preparar respuesta
            resultadoPago.setSuccess(true);
            resultadoPago.setNegociacionId(contratacion.getId());
            resultadoPago.setMessage("approved".equals(mpStatus)
                    ? "¡Pago aprobado! El servicio está activo."
                    : "pending".equals(mpStatus)
                            ? "Pago en proceso de verificación"
                            : "Pago rechazado");

            return ResponseEntity.ok(resultadoPago);

        } catch (Exception e) {
            log.error("❌ Error al procesar pago: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponseDTO.builder()
                            .success(false)
                            .error("Error al procesar el pago: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Obtener configuración de MercadoPago para el frontend (Bricks)
     */
    @GetMapping("/config")
    @ResponseBody
    public ResponseEntity<MercadoPagoConfigDTO> getConfig() {
        MercadoPagoConfigDTO config = MercadoPagoConfigDTO.builder()
                .publicKey(mercadoPagoService.getPublicKey())
                .locale("es-CO")
                .testMode(mercadoPagoService.isTestMode())
                .appUrl(mercadoPagoService.getAppUrl())
                .build();

        return ResponseEntity.ok(config);
    }

    /**
     * Crear asignación automática de entrenador al usuario tras pago aprobado
     */
    private void crearAsignacionAutomatica(Usuario usuario, Integer entrenadorId) {
        try {
            // Verificar si ya existe una asignación activa entre este usuario y entrenador
            Optional<AsignacionEntrenador> asignacionExistente = asignacionRepo
                    .findByUsuarioIdAndEntrenadorId(usuario.getId(), entrenadorId);

            if (asignacionExistente.isPresent()) {
                // Si ya existe pero está rechazada o pendiente, actualizarla a ACEPTADA
                AsignacionEntrenador asignacion = asignacionExistente.get();
                if (asignacion.getEstado() != AsignacionEntrenador.EstadoAsignacion.ACEPTADA) {
                    asignacion.setEstado(AsignacionEntrenador.EstadoAsignacion.ACEPTADA);
                    asignacion.setFechaAceptacion(LocalDateTime.now());
                    asignacion.setMensajeRespuesta("Asignación automática tras pago aprobado");
                    asignacionRepo.save(asignacion);
                }
            } else {
                // Crear nueva asignación
                AsignacionEntrenador nuevaAsignacion = new AsignacionEntrenador();
                nuevaAsignacion.setUsuario(usuario);

                // Buscar el entrenador
                Usuario entrenador = new Usuario();
                entrenador.setId(entrenadorId);
                nuevaAsignacion.setEntrenador(entrenador);

                nuevaAsignacion.setEstado(AsignacionEntrenador.EstadoAsignacion.ACEPTADA);
                nuevaAsignacion.setFechaSolicitud(LocalDateTime.now());
                nuevaAsignacion.setFechaAceptacion(LocalDateTime.now());
                nuevaAsignacion.setMensajeSolicitud("Asignación automática tras contratación pagada");
                nuevaAsignacion.setMensajeRespuesta("Asignación automática - Pago aprobado");

                asignacionRepo.save(nuevaAsignacion);
            }
        } catch (Exception e) {
            // Log del error pero no detener el proceso de pago
            System.err.println("Error al crear asignación automática: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
