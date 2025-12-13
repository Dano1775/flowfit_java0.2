package com.example.flowfit.service;

import com.example.flowfit.model.*;
import com.example.flowfit.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio de ESCROW - Sistema de retención de pagos anti-estafas
 * Protege tanto a usuarios como entrenadores hasta que ambos confirmen el
 * servicio
 */
@Service
public class EscrowService {

    @Autowired
    private PagoContratacionRepository pagoRepo;

    @Autowired
    private ContratacionEntrenadorRepository contratacionRepo;

    @Autowired
    private MensajeRepository mensajeRepo;

    @Autowired
    private ConversacionRepository conversacionRepo;

    /**
     * Usuario confirma que recibió el servicio correctamente
     */
    @Transactional
    public Map<String, Object> usuarioConfirmaServicio(Long pagoId, Integer usuarioId, String comentario) {
        Map<String, Object> response = new HashMap<>();

        try {
            PagoContratacion pago = pagoRepo.findById(pagoId)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

            if (!pago.getUsuarioId().equals(usuarioId)) {
                throw new RuntimeException("No tienes permiso para confirmar este servicio");
            }

            if (pago.getDisputaActiva()) {
                throw new RuntimeException("No puedes confirmar el servicio mientras hay una disputa activa");
            }

            pago.setUsuarioConfirmaServicio(true);
            pago.setFechaConfirmacionUsuario(LocalDateTime.now());

            // Si el entrenador ya confirmó, liberar fondos automáticamente
            if (pago.getEntrenadorConfirmaServicio()) {
                liberarFondos(pago);
                response.put("fondosLiberados", true);
            } else {
                pago.setEstadoEscrow(PagoContratacion.EstadoEscrow.ESPERANDO_ENTRENADOR);
                response.put("fondosLiberados", false);
                response.put("message", "Esperando confirmación del entrenador");
            }

            pagoRepo.save(pago);

            // Crear mensaje en el chat
            ContratacionEntrenador contratacion = pago.getContratacion();
            List<Conversacion> conversaciones = conversacionRepo
                    .findByUsuarioIdAndEntrenadorIdOrderByFechaUltimoMensajeDesc(
                            contratacion.getUsuarioId(), contratacion.getEntrenadorId());
            Conversacion conversacion = conversaciones.isEmpty() ? null : conversaciones.get(0);
            if (conversacion == null)
                throw new RuntimeException("Conversación no encontrada");

            Mensaje mensaje = new Mensaje();
            mensaje.setConversacionId(conversacion.getId());
            mensaje.setRemitenteId(usuarioId);
            mensaje.setTipoMensaje(Mensaje.TipoMensaje.CONFIRMACION_SERVICIO);
            mensaje.setContenido("✅ El usuario confirmó que recibió el servicio correctamente." +
                    (comentario != null ? "\n\n💬 Comentario: " + comentario : ""));
            mensaje.setMetadata("{\"pagoId\": " + pagoId + ", \"tipo\": \"confirmacion_usuario\"}");
            mensajeRepo.save(mensaje);

            response.put("success", true);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    /**
     * Entrenador confirma que cumplió con el servicio
     */
    @Transactional
    public Map<String, Object> entrenadorConfirmaServicio(Long pagoId, Integer entrenadorId, String comentario) {
        Map<String, Object> response = new HashMap<>();

        try {
            PagoContratacion pago = pagoRepo.findById(pagoId)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

            ContratacionEntrenador contratacion = pago.getContratacion();

            if (!contratacion.getEntrenadorId().equals(entrenadorId)) {
                throw new RuntimeException("No tienes permiso para confirmar este servicio");
            }

            if (pago.getDisputaActiva()) {
                throw new RuntimeException("No puedes confirmar el servicio mientras hay una disputa activa");
            }

            pago.setEntrenadorConfirmaServicio(true);
            pago.setFechaConfirmacionEntrenador(LocalDateTime.now());

            // Si el usuario ya confirmó, liberar fondos automáticamente
            if (pago.getUsuarioConfirmaServicio()) {
                liberarFondos(pago);
                response.put("fondosLiberados", true);
            } else {
                pago.setEstadoEscrow(PagoContratacion.EstadoEscrow.ESPERANDO_USUARIO);
                response.put("fondosLiberados", false);
                response.put("message", "Esperando confirmación del usuario");
            }

            pagoRepo.save(pago);

            // Crear mensaje en el chat
            Conversacion conversacion = conversacionRepo.findByUsuarioIdAndEntrenadorId(
                    contratacion.getUsuarioId(), contratacion.getEntrenadorId()).orElseThrow();

            Mensaje mensaje = new Mensaje();
            mensaje.setConversacionId(conversacion.getId());
            mensaje.setRemitenteId(entrenadorId);
            mensaje.setTipoMensaje(Mensaje.TipoMensaje.CONFIRMACION_SERVICIO);
            mensaje.setContenido("✅ El entrenador confirmó que cumplió con el servicio." +
                    (comentario != null ? "\n\n💬 Comentario: " + comentario : ""));
            mensaje.setMetadata("{\"pagoId\": " + pagoId + ", \"tipo\": \"confirmacion_entrenador\"}");
            mensajeRepo.save(mensaje);

            response.put("success", true);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    /**
     * Iniciar una disputa (usuario o entrenador)
     */
    @Transactional
    public Map<String, Object> iniciarDisputa(Long pagoId, Integer personaId, String razon) {
        Map<String, Object> response = new HashMap<>();

        try {
            PagoContratacion pago = pagoRepo.findById(pagoId)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

            ContratacionEntrenador contratacion = pago.getContratacion();

            boolean esUsuario = contratacion.getUsuarioId().equals(personaId);
            boolean esEntrenador = contratacion.getEntrenadorId().equals(personaId);

            if (!esUsuario && !esEntrenador) {
                throw new RuntimeException("No tienes permiso para iniciar una disputa en este pago");
            }

            if (pago.getDisputaActiva()) {
                throw new RuntimeException("Ya hay una disputa activa para este pago");
            }

            if (LocalDateTime.now().isAfter(pago.getFechaLimiteDisputa())) {
                throw new RuntimeException("El período para iniciar disputas ya expiró");
            }

            pago.setDisputaActiva(true);
            pago.setDisputaIniciadaPor(esUsuario ? PagoContratacion.DisputaIniciadaPor.USUARIO
                    : PagoContratacion.DisputaIniciadaPor.ENTRENADOR);
            pago.setDisputaRazon(razon);
            pago.setDisputaFecha(LocalDateTime.now());
            pago.setEstadoEscrow(PagoContratacion.EstadoEscrow.DISPUTA);
            pago.setEstadoPago(PagoContratacion.EstadoPago.EN_MEDIACION);

            pagoRepo.save(pago);

            // Crear mensaje en el chat
            List<Conversacion> conversaciones = conversacionRepo
                    .findByUsuarioIdAndEntrenadorIdOrderByFechaUltimoMensajeDesc(
                            contratacion.getUsuarioId(), contratacion.getEntrenadorId());
            Conversacion conversacion = conversaciones.isEmpty() ? null : conversaciones.get(0);
            if (conversacion == null)
                throw new RuntimeException("Conversación no encontrada");

            Mensaje mensaje = new Mensaje();
            mensaje.setConversacionId(conversacion.getId());
            mensaje.setRemitenteId(personaId);
            mensaje.setTipoMensaje(Mensaje.TipoMensaje.DISPUTA_INICIADA);
            mensaje.setContenido("⚠️ Se ha iniciado una DISPUTA.\n\n" +
                    "📋 Motivo: " + razon + "\n\n" +
                    "El equipo de FlowFit revisará el caso y tomará una decisión en las próximas 48 horas.");
            mensaje.setMetadata("{\"pagoId\": " + pagoId + ", \"tipo\": \"disputa_iniciada\"}");
            mensajeRepo.save(mensaje);

            response.put("success", true);
            response.put("message", "Disputa iniciada. El equipo de soporte la revisará pronto.");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    /**
     * Admin resuelve una disputa
     */
    @Transactional
    public Map<String, Object> resolverDisputa(Long pagoId, String resolucion, String decision) {
        Map<String, Object> response = new HashMap<>();

        try {
            PagoContratacion pago = pagoRepo.findById(pagoId)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

            if (!pago.getDisputaActiva()) {
                throw new RuntimeException("No hay una disputa activa para este pago");
            }

            pago.setDisputaResuelta(true);
            pago.setDisputaResolucion(resolucion);
            pago.setDisputaFechaResolucion(LocalDateTime.now());

            switch (decision.toUpperCase()) {
                case "LIBERAR":
                    liberarFondos(pago);
                    pago.setDisputaActiva(false);
                    response.put("decision", "Fondos liberados al entrenador");
                    break;

                case "REEMBOLSAR":
                    reembolsarUsuario(pago);
                    pago.setDisputaActiva(false);
                    response.put("decision", "Fondos reembolsados al usuario");
                    break;

                case "PARCIAL":
                    // En caso de resolución parcial, se manejará manualmente
                    pago.setEstadoPago(PagoContratacion.EstadoPago.EN_MEDIACION);
                    response.put("decision", "Resolución parcial - requiere procesamiento manual");
                    break;

                default:
                    throw new RuntimeException("Decisión no válida");
            }

            pagoRepo.save(pago);

            // Notificar a ambas partes
            ContratacionEntrenador contratacion = pago.getContratacion();
            List<Conversacion> conversaciones = conversacionRepo
                    .findByUsuarioIdAndEntrenadorIdOrderByFechaUltimoMensajeDesc(
                            contratacion.getUsuarioId(), contratacion.getEntrenadorId());
            Conversacion conversacion = conversaciones.isEmpty() ? null : conversaciones.get(0);
            if (conversacion == null)
                throw new RuntimeException("Conversación no encontrada");

            Mensaje mensaje = new Mensaje();
            mensaje.setConversacionId(conversacion.getId());
            mensaje.setRemitenteId(null); // Mensaje del sistema
            mensaje.setTipoMensaje(Mensaje.TipoMensaje.SISTEMA);
            mensaje.setContenido("⚖️ RESOLUCIÓN DE DISPUTA\n\n" +
                    "El equipo de FlowFit ha revisado el caso.\n\n" +
                    "📋 Resolución: " + resolucion);
            mensaje.setMetadata("{\"pagoId\": " + pagoId + ", \"tipo\": \"disputa_resuelta\"}");
            mensajeRepo.save(mensaje);

            response.put("success", true);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    /**
     * Liberar fondos al entrenador (después de confirmaciones o resolución de
     * disputa)
     */
    private void liberarFondos(PagoContratacion pago) {
        pago.setEstadoEscrow(PagoContratacion.EstadoEscrow.LIBERADO);
        pago.setFechaLiberacionFondos(LocalDateTime.now());
        pago.setEstadoPago(PagoContratacion.EstadoPago.APROBADO);

        // Aquí se integraría con MercadoPago para transferir fondos al entrenador
        // Por ahora solo actualizamos el estado
    }

    /**
     * Reembolsar dinero al usuario (en caso de disputa ganada)
     */
    private void reembolsarUsuario(PagoContratacion pago) {
        pago.setEstadoEscrow(PagoContratacion.EstadoEscrow.REEMBOLSADO);
        pago.setEstadoPago(PagoContratacion.EstadoPago.REEMBOLSADO);

        // Aquí se integraría con MercadoPago para procesar el reembolso
        // Por ahora solo actualizamos el estado
    }

    /**
     * Proceso automático: Liberar fondos después de 7 días si no hay disputa
     */
    @Transactional
    public void procesarLiberacionAutomatica() {
        List<PagoContratacion> pagos = pagoRepo.findPagosParaLiberacionAutomatica();

        for (PagoContratacion pago : pagos) {
            pago.setUsuarioConfirmaServicio(true);
            pago.setEntrenadorConfirmaServicio(true);
            liberarFondos(pago);
            pagoRepo.save(pago);

            // Notificar liberación automática
            ContratacionEntrenador contratacion = pago.getContratacion();
            List<Conversacion> conversaciones = conversacionRepo
                    .findByUsuarioIdAndEntrenadorIdOrderByFechaUltimoMensajeDesc(
                            contratacion.getUsuarioId(), contratacion.getEntrenadorId());
            Conversacion conversacion = conversaciones.isEmpty() ? null : conversaciones.get(0);

            if (conversacion != null) {
                Mensaje mensaje = new Mensaje();
                mensaje.setConversacionId(conversacion.getId());
                mensaje.setRemitenteId(null);
                mensaje.setTipoMensaje(Mensaje.TipoMensaje.SISTEMA);
                mensaje.setContenido(
                        "✅ Los fondos han sido liberados automáticamente al entrenador después de 7 días sin disputas.");
                mensaje.setMetadata("{\"pagoId\": " + pago.getId() + ", \"tipo\": \"liberacion_automatica\"}");
                mensajeRepo.save(mensaje);
            }
        }
    }

    /**
     * Obtener información del estado del escrow para un pago
     */
    public Map<String, Object> obtenerEstadoEscrow(Long pagoId) {
        Map<String, Object> info = new HashMap<>();

        PagoContratacion pago = pagoRepo.findById(pagoId).orElseThrow();

        info.put("estadoEscrow", pago.getEstadoEscrow().name());
        info.put("usuarioConfirmo", pago.getUsuarioConfirmaServicio());
        info.put("entrenadorConfirmo", pago.getEntrenadorConfirmaServicio());
        info.put("disputaActiva", pago.getDisputaActiva());
        info.put("fechaLimiteDisputa", pago.getFechaLimiteDisputa());
        info.put("diasRestantesDisputa",
                pago.getFechaLimiteDisputa() != null
                        ? java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), pago.getFechaLimiteDisputa())
                        : 0);

        if (pago.getDisputaActiva()) {
            info.put("disputaRazon", pago.getDisputaRazon());
            info.put("disputaIniciadaPor", pago.getDisputaIniciadaPor().name());
            info.put("disputaFecha", pago.getDisputaFecha());
        }

        return info;
    }
}
