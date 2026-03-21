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

    @Autowired
    private ChatService chatService;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

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

            log.info("Procesando pago con Bricks - Token: {}, Amount: {}, NegociacionId: {}",
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

            // Resolver conversación donde se debe reflejar el pago
            Conversacion conversacionPago = null;
            if (paymentRequest.getConversacionId() != null) {
                Optional<Conversacion> convById = conversacionRepo.findById(paymentRequest.getConversacionId());
                if (convById.isPresent()) {
                    Conversacion c = convById.get();
                    boolean ok = Objects.equals(c.getUsuarioId(), usuario.getId())
                            && Objects.equals(c.getEntrenadorId(), contratacion.getEntrenadorId());
                    if (ok) {
                        conversacionPago = c;
                    } else {
                        log.warn("ConversacionId {} no pertenece a usuario {} y entrenador {}",
                                c.getId(), usuario.getId(), contratacion.getEntrenadorId());
                    }
                }
            }
            if (conversacionPago == null) {
                conversacionPago = chatService.obtenerOCrearConversacion(usuario.getId(),
                        contratacion.getEntrenadorId());
            }

            // Guard: evitar pagos duplicados para la misma contratación
            Optional<PagoContratacion> pagoExistenteOpt = pagoRepo.findByContratacionId(contratacion.getId());
            PagoContratacion pagoAUsar = null;
            if (pagoExistenteOpt.isPresent()) {
                PagoContratacion pagoExistente = pagoExistenteOpt.get();
                PagoContratacion.EstadoPago estado = pagoExistente.getEstadoPago();

                // Seguridad: no permitir que otro usuario "reutilice" un pago existente
                if (pagoExistente.getUsuarioId() != null
                        && !Objects.equals(pagoExistente.getUsuarioId(), usuario.getId())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(PaymentResponseDTO.builder()
                                    .success(false)
                                    .status("conflict")
                                    .negociacionId(contratacion.getId())
                                    .message(
                                            "Ya existe un registro de pago para este servicio asociado a otro usuario.")
                                    .build());
                }

                if (estado == PagoContratacion.EstadoPago.APROBADO) {
                    // Limpieza para que NO reaparezcan propuestas/CTAs tras recargar
                    try {
                        String p1 = "\"tipo\":\"PAGO_PENDIENTE\"";
                        String p2 = "\"tipo\": \"PAGO_PENDIENTE\"";
                        mensajeRepo.softDeleteSistemaByMetadataPattern(conversacionPago.getId(), p1, p2);
                        List<Mensaje> propuestas = mensajeRepo.findPropuestasPorConversacion(conversacionPago.getId());
                        for (Mensaje msg : propuestas) {
                            if (!Boolean.TRUE.equals(msg.getEliminado())) {
                                msg.setEliminado(true);
                                mensajeRepo.save(msg);
                            }
                        }
                    } catch (Exception ignore) {
                        // best-effort
                    }

                    // Re-emitir un mensaje de pago aprobado al chat (por si faltaba)
                    try {
                        Mensaje msj = new Mensaje();
                        msj.setConversacionId(conversacionPago.getId());
                        msj.setRemitenteId(usuario.getId());
                        msj.setTipoMensaje(Mensaje.TipoMensaje.SISTEMA);
                        msj.setContenido("¡Pago aprobado exitosamente!\n\n" +
                                "Monto: $"
                                + String.format("%,.0f",
                                        pagoExistente.getMonto() != null ? pagoExistente.getMonto().doubleValue() : 0d)
                                + " " +
                                (pagoExistente.getMoneda() != null ? pagoExistente.getMoneda() : "COP") + "\n" +
                                "Pago realizado. Ya pueden empezar a entrenar.\n" +
                                "Importante: No vuelvas a pagar. Si ves esta confirmación, el pago ya fue registrado.\n"
                                +
                                "El dinero está retenido de forma segura hasta que ambos confirmen el servicio.");

                        Map<String, Object> md = new HashMap<>();
                        md.put("tipo", "PAGO_APROBADO");
                        md.put("pagoId", pagoExistente.getId());
                        md.put("contratacionId", contratacion.getId());
                        md.put("monto", pagoExistente.getMonto());
                        md.put("duracionDias", contratacion.getDuracionDiasAcordada());
                        md.put("status", "approved");
                        try {
                            msj.setMetadata(objectMapper.writeValueAsString(md));
                        } catch (Exception e) {
                            msj.setMetadata("{\"tipo\":\"PAGO_APROBADO\",\"pagoId\":" + pagoExistente.getId() + "}");
                        }

                        Mensaje guardado = mensajeRepo.save(msj);
                        messagingTemplate.convertAndSend(
                                "/topic/conversacion/" + conversacionPago.getId(),
                                new MensajeDTO(guardado));
                    } catch (Exception e) {
                        log.warn("No se pudo re-emitir PAGO_APROBADO: {}", e.getMessage());
                    }

                    Long mpId = null;
                    try {
                        if (pagoExistente.getMpPaymentId() != null) {
                            mpId = Long.valueOf(pagoExistente.getMpPaymentId());
                        }
                    } catch (Exception ignore) {
                        mpId = null;
                    }

                    return ResponseEntity.ok(PaymentResponseDTO.builder()
                            .success(true)
                            .id(mpId)
                            .status("approved")
                            .transactionAmount(
                                    pagoExistente.getMonto() != null ? pagoExistente.getMonto().doubleValue() : null)
                            .currencyId(pagoExistente.getMoneda() != null ? pagoExistente.getMoneda() : "COP")
                            .negociacionId(contratacion.getId())
                            .message("Este servicio ya tiene un pago aprobado. No vuelvas a pagar.")
                            .build());
                }

                // Bloquear solo si realmente hay un pago en proceso.
                // Si está PENDIENTE pero sin mpPaymentId, suele ser un placeholder (p.ej. se
                // creó preferencia/init_point)
                // y debe permitir procesar el pago con Bricks reutilizando el mismo registro.
                boolean tieneMpPaymentId = pagoExistente.getMpPaymentId() != null
                        && !pagoExistente.getMpPaymentId().isBlank();
                if (estado == PagoContratacion.EstadoPago.PROCESANDO
                        || (estado == PagoContratacion.EstadoPago.PENDIENTE && tieneMpPaymentId)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(PaymentResponseDTO.builder()
                                    .success(false)
                                    .status("pending")
                                    .negociacionId(contratacion.getId())
                                    .message("Ya existe un pago en proceso para este servicio. Espera la confirmación.")
                                    .build());
                }

                // Reutilizar el registro existente (evita duplicados por contratacion_id)
                pagoAUsar = pagoExistente;
            }

            // Procesar pago con MercadoPago usando el token de Bricks
            PaymentResponseDTO resultadoPago = mercadoPagoService.procesarPago(paymentRequest, contratacion);

            log.info("Pago procesado - Payment ID: {}, Status: {}",
                    resultadoPago.getId(), resultadoPago.getStatus());

            // Crear/actualizar registro de pago en la base de datos
            PagoContratacion pago = pagoAUsar != null ? pagoAUsar : new PagoContratacion();
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
            Conversacion conversacion = conversacionPago;

            // Si el pago fue aprobado, eliminar CTAs de pago pendiente obsoletas para
            // evitar confusión.
            if ("approved".equals(mpStatus)) {
                try {
                    String p1 = "\"tipo\":\"PAGO_PENDIENTE\"";
                    String p2 = "\"tipo\": \"PAGO_PENDIENTE\"";
                    mensajeRepo.softDeleteSistemaByMetadataPattern(conversacion.getId(), p1, p2);

                    // Invalidar propuestas anteriores (tras pago aprobado no debe reaparecer la
                    // propuesta ni permitir re-pago)
                    List<Mensaje> propuestas = mensajeRepo.findPropuestasPorConversacion(conversacion.getId());
                    for (Mensaje msg : propuestas) {
                        if (!Boolean.TRUE.equals(msg.getEliminado())) {
                            msg.setEliminado(true);
                            mensajeRepo.save(msg);
                        }
                    }
                } catch (Exception ex) {
                    log.warn("No se pudo invalidar PAGO_PENDIENTE para conversación {}: {}", conversacion.getId(),
                            ex.getMessage());
                }
            }

            Mensaje mensaje = new Mensaje();
            mensaje.setConversacionId(conversacion.getId());
            mensaje.setRemitenteId(usuario.getId());
            mensaje.setTipoMensaje(Mensaje.TipoMensaje.SISTEMA);

            if ("approved".equals(mpStatus)) {
                mensaje.setContenido("¡Pago aprobado exitosamente!\n\n" +
                        "Monto: $" + String.format("%,.0f", resultadoPago.getTransactionAmount()) + " COP\n" +
                        "Pago realizado. Ya pueden empezar a entrenar.\n" +
                        "Importante: No vuelvas a pagar. Si ves esta confirmación, el pago ya fue registrado.\n" +
                        "El dinero está retenido de forma segura hasta que ambos confirmen el servicio.\n" +
                        "Duración: " + contratacion.getDuracionDiasAcordada() + " días");
            } else if ("pending".equals(mpStatus)) {
                mensaje.setContenido("Pago en proceso\n\n" +
                        "Tu pago está siendo verificado. Te notificaremos cuando sea aprobado.");
            } else {
                mensaje.setContenido("Pago rechazado\n\n" +
                        "Hubo un problema al procesar tu pago. Por favor, intenta con otro método de pago.");
            }

            // Metadata para render en frontend (sistema de protección / estados)
            try {
                Map<String, Object> metadata = new HashMap<>();
                if ("approved".equals(mpStatus)) {
                    metadata.put("tipo", "PAGO_APROBADO");
                }
                metadata.put("pagoId", pago.getId());
                metadata.put("contratacionId", contratacion.getId());
                metadata.put("monto", resultadoPago.getTransactionAmount());
                metadata.put("duracionDias", contratacion.getDuracionDiasAcordada());
                metadata.put("paymentId", String.valueOf(resultadoPago.getId()));
                metadata.put("status", mpStatus);
                mensaje.setMetadata(objectMapper.writeValueAsString(metadata));
            } catch (Exception e) {
                // Fallback mínimo (incluye 'tipo' cuando aplique para render realtime)
                if ("approved".equals(mpStatus)) {
                    mensaje.setMetadata(
                            "{\"tipo\":\"PAGO_APROBADO\",\"pagoId\":" + pago.getId() +
                                    ",\"contratacionId\":" + contratacion.getId() +
                                    ",\"duracionDias\":" + contratacion.getDuracionDiasAcordada() +
                                    ",\"monto\":" + resultadoPago.getTransactionAmount() +
                                    ",\"status\":\"" + mpStatus + "\"}");
                } else {
                    mensaje.setMetadata("{\"pagoId\":" + pago.getId() + ",\"status\":\"" + mpStatus + "\"}");
                }
            }
            Mensaje mensajeGuardado = mensajeRepo.save(mensaje);

            // 🔔 NOTIFICACIÓN WEBSOCKET EN TIEMPO REAL (siempre: evita recargar)
            messagingTemplate.convertAndSend(
                    "/topic/conversacion/" + conversacion.getId(),
                    new MensajeDTO(mensajeGuardado));

            log.info("🔔 WebSocket enviado - pago {} status {} conversación {}",
                    pago.getId(), mpStatus, conversacion.getId());

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
