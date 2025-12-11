package com.example.flowfit.controller;

import com.example.flowfit.model.*;
import com.example.flowfit.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para confirmación de servicio completado y liberación de fondos
 */
@Slf4j
@Controller
@RequestMapping("/confirmacion")
public class ConfirmacionServicioController {

    @Autowired
    private PagoContratacionRepository pagoRepo;

    @Autowired
    private ContratacionEntrenadorRepository contratacionRepo;

    @Autowired
    private ConversacionRepository conversacionRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Usuario confirma que el servicio fue completado satisfactoriamente
     */
    @PostMapping("/usuario/{pagoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> usuarioConfirmaServicio(
            @PathVariable Long pagoId,
            HttpSession session) {

        log.info("🔄 Recibida petición de confirmación de usuario para pago {}", pagoId);

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                log.warn("❌ Sesión no válida para confirmación de pago {}", pagoId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Sesión no válida"));
            }

            log.info("✅ Usuario {} intentando confirmar pago {}", usuario.getId(), pagoId);

            PagoContratacion pago = pagoRepo.findById(pagoId)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

            // Verificar que el usuario es el dueño del pago
            if (!pago.getUsuarioId().equals(usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "No tienes permiso para confirmar este pago"));
            }

            // Verificar que el pago está aprobado
            if (pago.getEstadoPago() != PagoContratacion.EstadoPago.APROBADO) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "El pago debe estar aprobado para confirmar"));
            }

            // Verificar que no esté ya confirmado
            if (Boolean.TRUE.equals(pago.getUsuarioConfirmaServicio())) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Ya habías confirmado el servicio anteriormente",
                        "yaConfirmado", true));
            }

            // Registrar confirmación del usuario
            pago.setUsuarioConfirmaServicio(true);
            pago.setFechaConfirmacionUsuario(LocalDateTime.now());

            // Si el entrenador también confirmó, liberar fondos
            if (Boolean.TRUE.equals(pago.getEntrenadorConfirmaServicio())) {
                liberarFondos(pago);
            } else {
                pago.setEstadoEscrow(PagoContratacion.EstadoEscrow.ESPERANDO_ENTRENADOR);
            }

            pagoRepo.save(pago);

            // Notificar por WebSocket
            ContratacionEntrenador contratacion = contratacionRepo.findById(pago.getContratacionId())
                    .orElseThrow(() -> new RuntimeException("Contratación no encontrada"));

            Conversacion conversacion = conversacionRepo
                    .findByUsuarioIdAndEntrenadorId(usuario.getId(), contratacion.getEntrenadorId())
                    .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));

            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "CONFIRMACION_USUARIO");
            notificacion.put("pagoId", pago.getId());
            notificacion.put("usuarioConfirma", true);
            notificacion.put("entrenadorConfirma", pago.getEntrenadorConfirmaServicio());
            notificacion.put("estadoEscrow", pago.getEstadoEscrow().name());
            notificacion.put("mensaje", Boolean.TRUE.equals(pago.getEntrenadorConfirmaServicio())
                    ? "✅ ¡Ambos han confirmado! Los fondos han sido liberados al entrenador."
                    : "✅ Has confirmado el servicio. Esperando confirmación del entrenador...");
            notificacion.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend(
                    "/topic/conversacion/" + conversacion.getId(),
                    notificacion);

            log.info("✅ Usuario {} confirmó servicio para pago {}", usuario.getId(), pagoId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", Boolean.TRUE.equals(pago.getEntrenadorConfirmaServicio())
                    ? "¡Servicio confirmado! Los fondos han sido liberados."
                    : "Confirmación registrada. Esperando confirmación del entrenador.");
            response.put("ambosConfirmaron", Boolean.TRUE.equals(pago.getEntrenadorConfirmaServicio()));
            response.put("estadoEscrow", pago.getEstadoEscrow().name());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error al confirmar servicio (usuario): {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error al procesar confirmación: " + e.getMessage()));
        }
    }

    /**
     * Entrenador confirma que el servicio fue completado satisfactoriamente
     */
    @PostMapping("/entrenador/{pagoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> entrenadorConfirmaServicio(
            @PathVariable Long pagoId,
            HttpSession session) {

        log.info("🔄 Recibida petición de confirmación de entrenador para pago {}", pagoId);

        try {
            Usuario entrenador = (Usuario) session.getAttribute("usuario");
            if (entrenador == null) {
                log.warn("❌ Sesión no válida para confirmación de pago {}", pagoId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Sesión no válida"));
            }

            log.info("✅ Entrenador {} intentando confirmar pago {}", entrenador.getId(), pagoId);

            PagoContratacion pago = pagoRepo.findById(pagoId)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

            ContratacionEntrenador contratacion = contratacionRepo.findById(pago.getContratacionId())
                    .orElseThrow(() -> new RuntimeException("Contratación no encontrada"));

            // Verificar que el usuario es el entrenador de esta contratación
            if (!contratacion.getEntrenadorId().equals(entrenador.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "No tienes permiso para confirmar este pago"));
            }

            // Verificar que el pago está aprobado
            if (pago.getEstadoPago() != PagoContratacion.EstadoPago.APROBADO) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "El pago debe estar aprobado para confirmar"));
            }

            // Verificar que no esté ya confirmado
            if (Boolean.TRUE.equals(pago.getEntrenadorConfirmaServicio())) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Ya habías confirmado el servicio anteriormente",
                        "yaConfirmado", true));
            }

            // Registrar confirmación del entrenador
            pago.setEntrenadorConfirmaServicio(true);
            pago.setFechaConfirmacionEntrenador(LocalDateTime.now());

            // Si el usuario también confirmó, liberar fondos
            if (Boolean.TRUE.equals(pago.getUsuarioConfirmaServicio())) {
                liberarFondos(pago);
            } else {
                pago.setEstadoEscrow(PagoContratacion.EstadoEscrow.ESPERANDO_USUARIO);
            }

            pagoRepo.save(pago);

            // Notificar por WebSocket
            Conversacion conversacion = conversacionRepo
                    .findByUsuarioIdAndEntrenadorId(pago.getUsuarioId(), entrenador.getId())
                    .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));

            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "CONFIRMACION_ENTRENADOR");
            notificacion.put("pagoId", pago.getId());
            notificacion.put("usuarioConfirma", pago.getUsuarioConfirmaServicio());
            notificacion.put("entrenadorConfirma", true);
            notificacion.put("estadoEscrow", pago.getEstadoEscrow().name());
            notificacion.put("mensaje", Boolean.TRUE.equals(pago.getUsuarioConfirmaServicio())
                    ? "✅ ¡Ambos han confirmado! Los fondos han sido liberados al entrenador."
                    : "✅ El entrenador confirmó el servicio. Esperando tu confirmación...");
            notificacion.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend(
                    "/topic/conversacion/" + conversacion.getId(),
                    notificacion);

            log.info("✅ Entrenador {} confirmó servicio para pago {}", entrenador.getId(), pagoId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", Boolean.TRUE.equals(pago.getUsuarioConfirmaServicio())
                    ? "¡Servicio confirmado! Los fondos han sido liberados a tu cuenta."
                    : "Confirmación registrada. Esperando confirmación del usuario.");
            response.put("ambosConfirmaron", Boolean.TRUE.equals(pago.getUsuarioConfirmaServicio()));
            response.put("estadoEscrow", pago.getEstadoEscrow().name());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error al confirmar servicio (entrenador): {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error al procesar confirmación: " + e.getMessage()));
        }
    }

    /**
     * Obtener estado de confirmación de un pago
     */
    @GetMapping("/estado/{pagoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadoConfirmacion(
            @PathVariable Long pagoId,
            HttpSession session) {

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Sesión no válida"));
            }

            PagoContratacion pago = pagoRepo.findById(pagoId)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

            ContratacionEntrenador contratacion = contratacionRepo.findById(pago.getContratacionId())
                    .orElseThrow(() -> new RuntimeException("Contratación no encontrada"));

            // Verificar que el usuario tiene permiso para ver este pago
            boolean esUsuario = pago.getUsuarioId().equals(usuario.getId());
            boolean esEntrenador = contratacion.getEntrenadorId().equals(usuario.getId());

            if (!esUsuario && !esEntrenador) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "No tienes permiso para ver este pago"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pagoId", pago.getId());
            response.put("monto", pago.getMonto());
            response.put("estadoPago", pago.getEstadoPago().name());
            response.put("estadoEscrow", pago.getEstadoEscrow().name());
            response.put("usuarioConfirma", pago.getUsuarioConfirmaServicio());
            response.put("entrenadorConfirma", pago.getEntrenadorConfirmaServicio());
            response.put("fechaConfirmacionUsuario", pago.getFechaConfirmacionUsuario());
            response.put("fechaConfirmacionEntrenador", pago.getFechaConfirmacionEntrenador());
            response.put("fechaLiberacionFondos", pago.getFechaLiberacionFondos());
            response.put("ambosConfirmaron",
                    Boolean.TRUE.equals(pago.getUsuarioConfirmaServicio()) &&
                            Boolean.TRUE.equals(pago.getEntrenadorConfirmaServicio()));
            response.put("esUsuario", esUsuario);
            response.put("esEntrenador", esEntrenador);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error al obtener estado de confirmación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error al obtener estado: " + e.getMessage()));
        }
    }

    /**
     * Liberar fondos al entrenador cuando ambos confirman
     */
    private void liberarFondos(PagoContratacion pago) {
        log.info("💰 Liberando fondos para pago {} - Ambas partes confirmaron", pago.getId());

        pago.setEstadoEscrow(PagoContratacion.EstadoEscrow.LIBERADO);
        pago.setFechaLiberacionFondos(LocalDateTime.now());

        // Aquí podrías integrar con MercadoPago para hacer el transfer real
        // Por ahora solo marcamos como liberado en el sistema

        log.info("✅ Fondos liberados exitosamente para pago {}", pago.getId());
    }
}
