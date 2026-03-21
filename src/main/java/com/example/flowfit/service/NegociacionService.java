package com.example.flowfit.service;

import com.example.flowfit.dto.PropuestaDTO;
import com.example.flowfit.dto.MensajeDTO;
import com.example.flowfit.model.*;
import com.example.flowfit.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class NegociacionService {
    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private ContratacionEntrenadorRepository contratacionRepo;

    @Autowired
    private HistorialNegociacionRepository historialRepo;

    @Autowired
    private MensajeRepository mensajeRepo;

    @Autowired
    private ConversacionRepository conversacionRepo;

    @Autowired
    private PagoContratacionRepository pagoRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private List<Long> invalidarPropuestasActivas(Long conversacionId) {
        List<Long> invalidatedIds = new ArrayList<>();
        try {
            List<Mensaje> propuestas = mensajeRepo.findPropuestasPorConversacion(conversacionId);
            for (Mensaje msg : propuestas) {
                if (!Boolean.TRUE.equals(msg.getEliminado())) {
                    msg.setEliminado(true);
                    mensajeRepo.save(msg);
                    invalidatedIds.add(msg.getId());
                }
            }

            // También invalidar CTAs obsoletas de pago pendiente al llegar una nueva
            // propuesta.
            // (Evita que queden “Propuesta aceptada / proceder al pago” visibles cuando ya
            // hay una nueva negociación.)
            String p1 = "\"tipo\":\"PAGO_PENDIENTE\"";
            String p2 = "\"tipo\": \"PAGO_PENDIENTE\"";
            mensajeRepo.softDeleteSistemaByMetadataPattern(conversacionId, p1, p2);
        } catch (Exception e) {
            System.err.println("Error invalidando propuestas anteriores: " + e.getMessage());
        }
        return invalidatedIds;
    }

    /**
     * Entrenador envía propuesta inicial al usuario
     */
    @Transactional
    public Map<String, Object> enviarPropuestaInicial(Long conversacionId, Integer entrenadorId,
            PropuestaDTO propuesta) {
        Map<String, Object> response = new HashMap<>();

        try {
            Conversacion conversacion = conversacionRepo.findById(conversacionId)
                    .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));

            // Invalidar propuestas anteriores de esta conversación (si existen)
            List<Long> invalidatedIds = invalidarPropuestasActivas(conversacionId);

            // Crear contratación en estado NEGOCIACION
            ContratacionEntrenador contratacion = new ContratacionEntrenador();
            contratacion.setUsuarioId(conversacion.getUsuarioId());
            contratacion.setEntrenadorId(entrenadorId);
            contratacion.setPlanBaseId(propuesta.getPlanBaseId());
            contratacion.setEstado(ContratacionEntrenador.EstadoContratacion.NEGOCIACION);
            contratacion.setPrecioAcordado(propuesta.getPrecio());
            contratacion.setDuracionDiasAcordada(propuesta.getDuracionDias());
            contratacion.setRutinasMesAcordadas(propuesta.getRutinasMes());
            contratacion.setSeguimientoSemanalAcordado(propuesta.isSeguimientoSemanal());
            contratacion.setVideollamadasMesAcordadas(propuesta.getVideollamadasMes());
            contratacion.setPlanNutricionalAcordado(propuesta.isPlanNutricional());
            contratacion.setChatDirectoAcordado(propuesta.isChatDirecto());
            contratacion.setServiciosAdicionales(propuesta.getServiciosAdicionales());
            contratacion.setVersionNegociacion(1);
            contratacion.setUltimaPropuestaDe(ContratacionEntrenador.PropuestaDe.ENTRENADOR);
            contratacion.setNotaEntrenador(propuesta.getMensaje());

            contratacionRepo.save(contratacion);

            // Validar duracionDias - poner 30 por defecto si viene null
            Integer duracionDias = propuesta.getDuracionDias();
            if (duracionDias == null || duracionDias <= 0) {
                duracionDias = 30; // 30 días por defecto
            }

            // Crear historial de negociación
            HistorialNegociacion historial = new HistorialNegociacion();
            historial.setContratacionId(contratacion.getId());
            historial.setVersion(1);
            historial.setPropuestoPor(HistorialNegociacion.PropuestoPor.ENTRENADOR);
            historial.setPrecioPropuesto(propuesta.getPrecio());
            historial.setPrecioBaseReferencia(propuesta.getPrecio()); // Precio base de referencia
            historial.setDuracionPropuesta(duracionDias);
            historial.setServiciosPropuestos(crearJsonServicios(propuesta));
            historial.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.PENDIENTE);
            historial.setMensaje(propuesta.getMensaje());

            historialRepo.save(historial);

            // COMENTADO - Ya no usamos SDK, ahora Bricks maneja el pago en el frontend
            // String linkPago = mercadoPagoService.crearPreferenciaPago(
            // contratacion.getId(), propuesta.getPrecio(), "Plan personalizado FlowFit",
            // "Propuesta de plan enviada por entrenador");
            String linkPago = "#pagar"; // Link temporal, el pago se hace con Bricks

            // Crear mensaje especial en el chat
            Mensaje mensaje = new Mensaje();
            mensaje.setConversacionId(conversacionId);
            mensaje.setRemitenteId(entrenadorId);
            mensaje.setTipoMensaje(Mensaje.TipoMensaje.PROPUESTA_PLAN);
            mensaje.setContenido(generarTextoPropuesta(propuesta));
            mensaje.setMetadata(
                    crearMetadataPropuesta(contratacion.getId(), historial.getId(), 1, propuesta, linkPago));

            mensajeRepo.save(mensaje);

            // Enviar notificación en tiempo real vía WebSocket
            try {
                Map<String, Object> mensajeWs = new HashMap<>();
                mensajeWs.put("id", mensaje.getId());
                mensajeWs.put("conversacionId", mensaje.getConversacionId());
                mensajeWs.put("remitenteId", mensaje.getRemitenteId());
                mensajeWs.put("contenido", mensaje.getContenido());
                mensajeWs.put("tipoMensaje", "PROPUESTA_PLAN");
                mensajeWs.put("fechaEnvio", mensaje.getFechaEnvio().toString());
                mensajeWs.put("metadata", objectMapper.readValue(mensaje.getMetadata(), Map.class));
                mensajeWs.put("invalidatedMensajeIds", invalidatedIds);

                messagingTemplate.convertAndSend("/topic/conversacion/" + conversacionId, mensajeWs);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                System.err.println("Error al parsear metadata para WebSocket: " + e.getMessage());
            }

            response.put("success", true);
            response.put("message", "Propuesta enviada correctamente");
            response.put("contratacionId", contratacion.getId());
            response.put("mensajeId", mensaje.getId());
            response.put("linkPago", linkPago);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al enviar la propuesta inicial: " + e.getMessage());
            e.printStackTrace(); // Imprimir el stack trace para depuración
            // No relanzar la excepción para permitir que la respuesta JSON se envíe al
            // cliente.
        }

        return response;
    }

    /**
     * Usuario responde a una propuesta (ACEPTAR, RECHAZAR, CONTRAOFERTA)
     */
    @Transactional
    public Map<String, Object> responderPropuesta(Long contratacionId, Integer usuarioId, String accion,
            PropuestaDTO contraoferta) {
        System.out.println("=== RESPONDER PROPUESTA ===");
        System.out.println("ContratacionId: " + contratacionId);
        System.out.println("UsuarioId: " + usuarioId);
        System.out.println("Acción: " + accion);

        ContratacionEntrenador contratacion = contratacionRepo.findById(contratacionId)
                .orElseThrow(() -> new RuntimeException("Contratación no encontrada"));

        // Validar que sea el usuario correcto
        if (!contratacion.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para responder esta propuesta");
        }

        Map<String, Object> response;
        switch (accion.toUpperCase()) {
            case "ACEPTAR":
                System.out.println("Ejecutando aceptarPropuesta...");
                response = aceptarPropuesta(contratacion, usuarioId);
                break;

            case "RECHAZAR":
                System.out.println("Ejecutando rechazarPropuesta...");
                response = rechazarPropuesta(contratacion, usuarioId,
                        contraoferta != null ? contraoferta.getMensaje() : null);
                break;

            case "CONTRAOFERTA":
                System.out.println("Ejecutando enviarContraoferta...");
                response = enviarContraoferta(contratacion, usuarioId, contraoferta);
                break;

            default:
                throw new RuntimeException("Acción no válida");
        }

        System.out.println("Response: " + response);
        return response;
    }

    /**
     * Entrenador responde a una contraoferta del usuario
     */
    @Transactional
    public Map<String, Object> entrenadorRespondeContraoferta(Long contratacionId, Integer entrenadorId, String accion,
            PropuestaDTO nuevaPropuesta) {
        ContratacionEntrenador contratacion = contratacionRepo.findById(contratacionId)
                .orElseThrow(() -> new RuntimeException("Contratación no encontrada"));

        if (!contratacion.getEntrenadorId().equals(entrenadorId)) {
            throw new RuntimeException("No tienes permiso para responder esta propuesta");
        }

        switch (accion.toUpperCase()) {
            case "ACEPTAR":
                return entrenadorAceptaContraoferta(contratacion, entrenadorId);

            case "RECHAZAR":
                return rechazarPropuesta(contratacion, entrenadorId,
                        nuevaPropuesta != null ? nuevaPropuesta.getMensaje() : null);

            case "CONTRAOFERTA":
                return enviarContraofertaEntrenador(contratacion, entrenadorId, nuevaPropuesta);

            default:
                throw new RuntimeException("Acción no válida");
        }
    }

    // ===== MÉTODOS PRIVADOS =====

    private Map<String, Object> aceptarPropuesta(ContratacionEntrenador contratacion, Integer usuarioId) {
        Map<String, Object> response = new HashMap<>();

        // Cambiar estado a PENDIENTE_PAGO
        contratacion.setEstado(ContratacionEntrenador.EstadoContratacion.PENDIENTE_PAGO);
        contratacion.setFechaAprobacion(LocalDateTime.now());
        contratacionRepo.save(contratacion);

        // Actualizar historial
        HistorialNegociacion ultimaPropuesta = historialRepo
                .findTopByContratacionIdOrderByVersionDesc(contratacion.getId());
        if (ultimaPropuesta != null) {
            ultimaPropuesta.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.ACEPTADA);
            ultimaPropuesta.setFechaRespuesta(LocalDateTime.now());
            historialRepo.save(ultimaPropuesta);
        }

        // Obtener precio de la última propuesta o de la contratación
        BigDecimal precioFinal = contratacion.getPrecioAcordado();
        if (precioFinal == null && ultimaPropuesta != null) {
            precioFinal = ultimaPropuesta.getPrecioPropuesto();
        }

        // Asegurar monto mínimo (MercadoPago requiere mínimo 3,000 COP)
        if (precioFinal == null || precioFinal.compareTo(BigDecimal.valueOf(3000)) < 0) {
            System.out.println("⚠️ Precio menor al mínimo. Ajustando a 3,000 COP");
            precioFinal = BigDecimal.valueOf(3000);
        }

        System.out.println("💰 Precio final calculado: " + precioFinal);

        // CREAR REGISTRO DE PAGO AUTOMÁTICAMENTE
        PagoContratacion pagoExistente = pagoRepo.findByContratacionId(contratacion.getId()).orElse(null);

        if (pagoExistente == null) {
            System.out.println("💳 Creando registro de pago para contratación: " + contratacion.getId());

            PagoContratacion nuevoPago = new PagoContratacion();
            nuevoPago.setContratacionId(contratacion.getId());
            nuevoPago.setUsuarioId(contratacion.getUsuarioId());
            nuevoPago.setMonto(precioFinal);
            nuevoPago.setMoneda("COP");
            nuevoPago.setEstadoPago(PagoContratacion.EstadoPago.PENDIENTE);
            nuevoPago.setEstadoEscrow(PagoContratacion.EstadoEscrow.RETENIDO);
            nuevoPago.setFechaCreacion(LocalDateTime.now());

            pagoRepo.save(nuevoPago);
            System.out.println("✅ Pago creado exitosamente con ID: " + nuevoPago.getId());

            response.put("pagoId", nuevoPago.getId());
        } else {
            System.out.println("ℹ️ Ya existe un pago para esta contratación");
            response.put("pagoId", pagoExistente.getId());
        }

        // Crear mensaje de pago en el chat
        Optional<Conversacion> conversacionOpt = conversacionRepo
                .findByUsuarioIdAndEntrenadorId(contratacion.getUsuarioId(), contratacion.getEntrenadorId());

        if (conversacionOpt.isPresent()) {
            Conversacion conversacion = conversacionOpt.get();

            // Eliminar TODAS las propuestas anteriores del chat
            List<Mensaje> mensajesAnteriores = mensajeRepo
                    .findByConversacionIdOrderByFechaEnvioAsc(conversacion.getId());
            for (Mensaje msg : mensajesAnteriores) {
                Boolean eliminado = msg.getEliminado();
                if (msg.getTipoMensaje() == Mensaje.TipoMensaje.PROPUESTA_PLAN && (eliminado == null || !eliminado)) {
                    msg.setEliminado(true);
                    mensajeRepo.save(msg);
                    System.out.println("🗑️ Mensaje de propuesta eliminado después de aceptación");
                }
            }

            // Crear mensaje con información del pago
            Mensaje mensajePago = new Mensaje();
            mensajePago.setConversacionId(conversacion.getId());
            mensajePago.setRemitenteId(null); // Sistema
            mensajePago.setTipoMensaje(Mensaje.TipoMensaje.SISTEMA);
            mensajePago.setContenido("✅ Propuesta aceptada. Procede con el pago para confirmar la contratación.");

            // Metadata con información del pago
            Map<String, Object> pagoMetadata = new HashMap<>();
            pagoMetadata.put("tipo", "PAGO_PENDIENTE");
            pagoMetadata.put("contratacionId", contratacion.getId());
            pagoMetadata.put("precioFinal", precioFinal.doubleValue());
            pagoMetadata.put("duracionDias", contratacion.getDuracionDiasAcordada());
            Long pagoId = (Long) response.get("pagoId");
            if (pagoId != null) {
                pagoMetadata.put("pagoId", pagoId);
            }

            try {
                mensajePago.setMetadata(objectMapper.writeValueAsString(pagoMetadata));
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                System.err.println("Error al crear metadata: " + e.getMessage());
                mensajePago.setMetadata("{}"); // Metadata vacío como fallback
            }

            mensajeRepo.save(mensajePago);

            // Notificar vía WebSocket con el DTO del mensaje del sistema
            messagingTemplate.convertAndSend("/topic/conversacion/" + conversacion.getId(),
                    new com.example.flowfit.dto.MensajeDTO(mensajePago));
        } else {
            System.err.println("⚠️ No se encontró conversación para usuario " + contratacion.getUsuarioId() +
                    " y entrenador " + contratacion.getEntrenadorId());
        }

        response.put("success", true);
        response.put("message", "Propuesta aceptada. Redirigiendo al pago...");
        response.put("contratacionId", contratacion.getId());
        response.put("requierePago", true);
        response.put("precioFinal", precioFinal.doubleValue());
        response.put("duracionDias", contratacion.getDuracionDiasAcordada());
        response.put("nombrePlan", ultimaPropuesta != null && ultimaPropuesta.getServiciosPropuestos() != null
                ? "Plan Personalizado"
                : "Plan de Entrenamiento");

        return response;
    }

    private Map<String, Object> entrenadorAceptaContraoferta(ContratacionEntrenador contratacion,
            Integer entrenadorId) {
        Map<String, Object> response = new HashMap<>();

        // Cambiar estado a PENDIENTE_PAGO
        contratacion.setEstado(ContratacionEntrenador.EstadoContratacion.PENDIENTE_PAGO);
        contratacion.setFechaAprobacion(LocalDateTime.now());
        contratacionRepo.save(contratacion);

        // Actualizar historial
        HistorialNegociacion ultimaPropuesta = historialRepo
                .findTopByContratacionIdOrderByVersionDesc(contratacion.getId());
        if (ultimaPropuesta != null) {
            ultimaPropuesta.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.ACEPTADA);
            ultimaPropuesta.setFechaRespuesta(LocalDateTime.now());
            historialRepo.save(ultimaPropuesta);
        }

        // Obtener precio de la última propuesta o de la contratación
        BigDecimal precioFinal = contratacion.getPrecioAcordado();
        if (precioFinal == null && ultimaPropuesta != null) {
            precioFinal = ultimaPropuesta.getPrecioPropuesto();
        }

        // Asegurar monto mínimo
        if (precioFinal == null || precioFinal.compareTo(BigDecimal.valueOf(3000)) < 0) {
            System.out.println("⚠️ Precio menor al mínimo. Ajustando a 3,000 COP");
            precioFinal = BigDecimal.valueOf(3000);
        }

        System.out.println("💰 Precio final calculado (entrenador): " + precioFinal);

        // CREAR REGISTRO DE PAGO AUTOMÁTICAMENTE
        PagoContratacion pagoExistente = pagoRepo.findByContratacionId(contratacion.getId()).orElse(null);

        if (pagoExistente == null) {
            System.out.println("💳 Creando registro de pago para contratación: " + contratacion.getId());

            PagoContratacion nuevoPago = new PagoContratacion();
            nuevoPago.setContratacionId(contratacion.getId());
            nuevoPago.setUsuarioId(contratacion.getUsuarioId());
            nuevoPago.setMonto(precioFinal);
            nuevoPago.setMoneda("COP");
            nuevoPago.setEstadoPago(PagoContratacion.EstadoPago.PENDIENTE);
            nuevoPago.setEstadoEscrow(PagoContratacion.EstadoEscrow.RETENIDO);
            nuevoPago.setFechaCreacion(LocalDateTime.now());

            pagoRepo.save(nuevoPago);
            System.out.println("✅ Pago creado exitosamente con ID: " + nuevoPago.getId());

            response.put("pagoId", nuevoPago.getId());
        } else {
            System.out.println("ℹ️ Ya existe un pago para esta contratación");
            response.put("pagoId", pagoExistente.getId());
        }

        // Crear mensaje de pago en el chat
        Optional<Conversacion> conversacionOpt = conversacionRepo
                .findByUsuarioIdAndEntrenadorId(contratacion.getUsuarioId(), contratacion.getEntrenadorId());

        if (conversacionOpt.isPresent()) {
            Conversacion conversacion = conversacionOpt.get();

            // Crear mensaje con información del pago
            Mensaje mensajePago = new Mensaje();
            mensajePago.setConversacionId(conversacion.getId());
            mensajePago.setRemitenteId(null); // Sistema
            mensajePago.setTipoMensaje(Mensaje.TipoMensaje.SISTEMA);
            mensajePago.setContenido("✅ Propuesta aceptada por el entrenador. El usuario puede proceder con el pago.");

            // Metadata con información del pago
            Map<String, Object> pagoMetadata = new HashMap<>();
            pagoMetadata.put("tipo", "PAGO_PENDIENTE");
            pagoMetadata.put("contratacionId", contratacion.getId());
            pagoMetadata.put("precioFinal", precioFinal.doubleValue());
            pagoMetadata.put("duracionDias", contratacion.getDuracionDiasAcordada());
            Long pagoId = (Long) response.get("pagoId");
            if (pagoId != null) {
                pagoMetadata.put("pagoId", pagoId);
            }

            try {
                mensajePago.setMetadata(objectMapper.writeValueAsString(pagoMetadata));
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                System.err.println("Error al crear metadata: " + e.getMessage());
                mensajePago.setMetadata("{}"); // Metadata vacío como fallback
            }

            mensajeRepo.save(mensajePago);

            // Notificar vía WebSocket con el DTO del mensaje del sistema
            messagingTemplate.convertAndSend("/topic/conversacion/" + conversacion.getId(),
                    new com.example.flowfit.dto.MensajeDTO(mensajePago));
        }

        response.put("success", true);
        response.put("message", "Contraoferta aceptada. El usuario puede proceder al pago.");
        response.put("contratacionId", contratacion.getId());
        response.put("precioFinal", precioFinal.doubleValue());
        response.put("requierePago", true);

        return response;
    }

    private Map<String, Object> rechazarPropuesta(ContratacionEntrenador contratacion, Integer usuarioId,
            String motivo) {
        Map<String, Object> response = new HashMap<>();

        // Cambiar estado
        contratacion.setEstado(ContratacionEntrenador.EstadoContratacion.RECHAZADA);
        if (contratacion.getUsuarioId().equals(usuarioId)) {
            contratacion.setNotaUsuario(motivo);
        } else {
            contratacion.setNotaEntrenador(motivo);
        }
        contratacionRepo.save(contratacion);

        // Actualizar historial
        HistorialNegociacion ultimaPropuesta = historialRepo
                .findTopByContratacionIdOrderByVersionDesc(contratacion.getId());
        if (ultimaPropuesta != null) {
            ultimaPropuesta.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.RECHAZADA);
            ultimaPropuesta.setFechaRespuesta(LocalDateTime.now());
            historialRepo.save(ultimaPropuesta);
        }

        response.put("success", true);
        response.put("message", "Propuesta rechazada");

        return response;
    }

    private Map<String, Object> enviarContraoferta(ContratacionEntrenador contratacion, Integer usuarioId,
            PropuestaDTO contraoferta) {
        Map<String, Object> response = new HashMap<>();

        // Validar máximo de versiones
        if (contratacion.getVersionNegociacion() >= 5) {
            throw new RuntimeException(
                    "Se alcanzó el límite de contraofertas (5 versiones). " +
                            "Acepta la propuesta actual o recházala para terminar la negociación.");
        }

        // Incrementar versión
        int nuevaVersion = contratacion.getVersionNegociacion() + 1;
        contratacion.setVersionNegociacion(nuevaVersion);
        contratacion.setUltimaPropuestaDe(ContratacionEntrenador.PropuestaDe.USUARIO);
        contratacion.setPrecioAcordado(contraoferta.getPrecio());
        contratacion.setDuracionDiasAcordada(contraoferta.getDuracionDias());
        contratacion.setRutinasMesAcordadas(contraoferta.getRutinasMes());
        contratacion.setSeguimientoSemanalAcordado(contraoferta.isSeguimientoSemanal());
        contratacion.setVideollamadasMesAcordadas(contraoferta.getVideollamadasMes());
        contratacion.setPlanNutricionalAcordado(contraoferta.isPlanNutricional());
        contratacion.setNotaUsuario(contraoferta.getMensaje());
        contratacionRepo.save(contratacion);

        // Marcar propuesta anterior como contraoferta
        HistorialNegociacion propuestaAnterior = historialRepo
                .findTopByContratacionIdOrderByVersionDesc(contratacion.getId());
        if (propuestaAnterior != null) {
            propuestaAnterior.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.CONTRAOFERTA);
            propuestaAnterior.setFechaRespuesta(LocalDateTime.now());
            historialRepo.save(propuestaAnterior);

            // Eliminar el mensaje anterior de la propuesta en el chat
            Conversacion conv = conversacionRepo
                    .findByUsuarioIdAndEntrenadorId(contratacion.getUsuarioId(), contratacion.getEntrenadorId())
                    .orElseThrow();
            List<Mensaje> mensajesAnteriores = mensajeRepo.findByConversacionIdOrderByFechaEnvioAsc(conv.getId());

            for (Mensaje msg : mensajesAnteriores) {
                Boolean eliminado = msg.getEliminado();
                if (msg.getTipoMensaje() == Mensaje.TipoMensaje.PROPUESTA_PLAN && (eliminado == null || !eliminado)) {
                    try {
                        Map<String, Object> metadata = objectMapper.readValue(msg.getMetadata(), Map.class);
                        Integer versionMsg = (Integer) metadata.get("version");
                        if (versionMsg != null && versionMsg == (nuevaVersion - 1)) {
                            msg.setEliminado(true);
                            mensajeRepo.save(msg);
                            System.out.println(
                                    "🗑️ Mensaje de propuesta anterior (v" + versionMsg + ") marcado como eliminado");
                            break;
                        }
                    } catch (Exception e) {
                        System.err.println("Error al procesar metadata del mensaje: " + e.getMessage());
                    }
                }
            }
        }

        // Crear nuevo historial
        HistorialNegociacion nuevoHistorial = new HistorialNegociacion();
        nuevoHistorial.setContratacionId(contratacion.getId());
        nuevoHistorial.setVersion(nuevaVersion);
        nuevoHistorial.setPropuestoPor(HistorialNegociacion.PropuestoPor.USUARIO);
        nuevoHistorial.setPrecioPropuesto(contraoferta.getPrecio());
        // Usar precio anterior como referencia, o el nuevo precio si no hay anterior
        BigDecimal precioBase = (propuestaAnterior != null) ? propuestaAnterior.getPrecioPropuesto()
                : contraoferta.getPrecio();
        nuevoHistorial.setPrecioBaseReferencia(precioBase);
        nuevoHistorial.setDuracionPropuesta(contraoferta.getDuracionDias());
        nuevoHistorial.setServiciosPropuestos(crearJsonServicios(contraoferta));
        nuevoHistorial.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.PENDIENTE);
        nuevoHistorial.setMensaje(contraoferta.getMensaje());
        historialRepo.save(nuevoHistorial);

        // Crear mensaje en chat
        Conversacion conversacion = conversacionRepo
                .findByUsuarioIdAndEntrenadorId(contratacion.getUsuarioId(), contratacion.getEntrenadorId())
                .orElseThrow();

        // Invalidar propuestas anteriores (dejar solo la más reciente activa)
        List<Long> invalidatedIds = invalidarPropuestasActivas(conversacion.getId());

        Mensaje mensaje = new Mensaje();
        mensaje.setConversacionId(conversacion.getId());
        mensaje.setRemitenteId(usuarioId);
        mensaje.setTipoMensaje(Mensaje.TipoMensaje.PROPUESTA_PLAN);
        mensaje.setContenido(generarTextoPropuesta(contraoferta));
        mensaje.setMetadata(
                crearMetadataPropuesta(contratacion.getId(), nuevoHistorial.getId(), nuevaVersion, contraoferta, null));
        mensajeRepo.save(mensaje);

        // Enviar por WebSocket
        try {
            Map<String, Object> mensajeWs = new HashMap<>();
            mensajeWs.put("id", mensaje.getId());
            mensajeWs.put("conversacionId", mensaje.getConversacionId());
            mensajeWs.put("remitenteId", mensaje.getRemitenteId());
            mensajeWs.put("contenido", mensaje.getContenido());
            mensajeWs.put("tipoMensaje", "PROPUESTA_PLAN");
            mensajeWs.put("fechaEnvio", mensaje.getFechaEnvio().toString());
            mensajeWs.put("metadata", objectMapper.readValue(mensaje.getMetadata(), Map.class));
            mensajeWs.put("invalidatedMensajeIds", invalidatedIds);

            messagingTemplate.convertAndSend("/topic/conversacion/" + conversacion.getId(), mensajeWs);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("Error al parsear metadata para WebSocket: " + e.getMessage());
        }

        response.put("success", true);
        response.put("message", "Contraoferta enviada al entrenador");
        response.put("version", nuevaVersion);
        response.put("mensajeId", mensaje.getId());

        return response;
    }

    private Map<String, Object> enviarContraofertaEntrenador(ContratacionEntrenador contratacion, Integer entrenadorId,
            PropuestaDTO contraoferta) {
        Map<String, Object> response = new HashMap<>();

        // Validar máximo de versiones
        if (contratacion.getVersionNegociacion() >= 5) {
            throw new RuntimeException(
                    "Se alcanzó el límite de contraofertas (5 versiones). " +
                            "Acepta la propuesta actual o recházala para terminar la negociación.");
        }

        // Incrementar versión
        int nuevaVersion = contratacion.getVersionNegociacion() + 1;
        contratacion.setVersionNegociacion(nuevaVersion);
        contratacion.setUltimaPropuestaDe(ContratacionEntrenador.PropuestaDe.ENTRENADOR);
        contratacion.setPrecioAcordado(contraoferta.getPrecio());
        contratacion.setDuracionDiasAcordada(contraoferta.getDuracionDias());
        contratacion.setRutinasMesAcordadas(contraoferta.getRutinasMes());
        contratacion.setSeguimientoSemanalAcordado(contraoferta.isSeguimientoSemanal());
        contratacion.setVideollamadasMesAcordadas(contraoferta.getVideollamadasMes());
        contratacion.setPlanNutricionalAcordado(contraoferta.isPlanNutricional());
        contratacion.setNotaEntrenador(contraoferta.getMensaje());
        contratacionRepo.save(contratacion);

        // Marcar propuesta anterior como contraoferta
        HistorialNegociacion propuestaAnterior = historialRepo
                .findTopByContratacionIdOrderByVersionDesc(contratacion.getId());
        if (propuestaAnterior != null) {
            propuestaAnterior.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.CONTRAOFERTA);
            propuestaAnterior.setFechaRespuesta(LocalDateTime.now());
            historialRepo.save(propuestaAnterior);

            // Eliminar el mensaje anterior de la propuesta en el chat
            Conversacion conv = conversacionRepo
                    .findByUsuarioIdAndEntrenadorId(contratacion.getUsuarioId(), contratacion.getEntrenadorId())
                    .orElseThrow();
            List<Mensaje> mensajesAnteriores = mensajeRepo.findByConversacionIdOrderByFechaEnvioAsc(conv.getId());

            for (Mensaje msg : mensajesAnteriores) {
                Boolean eliminado = msg.getEliminado();
                if (msg.getTipoMensaje() == Mensaje.TipoMensaje.PROPUESTA_PLAN && (eliminado == null || !eliminado)) {
                    try {
                        Map<String, Object> metadata = objectMapper.readValue(msg.getMetadata(), Map.class);
                        Integer versionMsg = (Integer) metadata.get("version");
                        if (versionMsg != null && versionMsg == (nuevaVersion - 1)) {
                            msg.setEliminado(true);
                            mensajeRepo.save(msg);
                            System.out.println(
                                    "🗑️ Mensaje de propuesta anterior (v" + versionMsg + ") marcado como eliminado");
                            break;
                        }
                    } catch (Exception e) {
                        System.err.println("Error al procesar metadata del mensaje: " + e.getMessage());
                    }
                }
            }
        }

        // Crear nuevo historial
        HistorialNegociacion nuevoHistorial = new HistorialNegociacion();
        nuevoHistorial.setContratacionId(contratacion.getId());
        nuevoHistorial.setVersion(nuevaVersion);
        nuevoHistorial.setPropuestoPor(HistorialNegociacion.PropuestoPor.ENTRENADOR);
        nuevoHistorial.setPrecioPropuesto(contraoferta.getPrecio());
        // Usar precio anterior como referencia, o el nuevo precio si no hay anterior
        BigDecimal precioBase = (propuestaAnterior != null) ? propuestaAnterior.getPrecioPropuesto()
                : contraoferta.getPrecio();
        nuevoHistorial.setPrecioBaseReferencia(precioBase);
        nuevoHistorial.setDuracionPropuesta(contraoferta.getDuracionDias());
        nuevoHistorial.setServiciosPropuestos(crearJsonServicios(contraoferta));
        nuevoHistorial.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.PENDIENTE);
        nuevoHistorial.setMensaje(contraoferta.getMensaje());
        historialRepo.save(nuevoHistorial);

        // Crear mensaje en chat
        Conversacion conversacion = conversacionRepo
                .findByUsuarioIdAndEntrenadorId(contratacion.getUsuarioId(), contratacion.getEntrenadorId())
                .orElseThrow();

        // Invalidar propuestas anteriores (dejar solo la más reciente activa)
        List<Long> invalidatedIds = invalidarPropuestasActivas(conversacion.getId());

        Mensaje mensaje = new Mensaje();
        mensaje.setConversacionId(conversacion.getId());
        mensaje.setRemitenteId(entrenadorId);
        mensaje.setTipoMensaje(Mensaje.TipoMensaje.PROPUESTA_PLAN);
        mensaje.setContenido(generarTextoPropuesta(contraoferta));
        mensaje.setMetadata(
                crearMetadataPropuesta(contratacion.getId(), nuevoHistorial.getId(), nuevaVersion, contraoferta, null));
        mensajeRepo.save(mensaje);

        // Enviar por WebSocket
        try {
            Map<String, Object> mensajeWs = new HashMap<>();
            mensajeWs.put("id", mensaje.getId());
            mensajeWs.put("conversacionId", mensaje.getConversacionId());
            mensajeWs.put("remitenteId", mensaje.getRemitenteId());
            mensajeWs.put("contenido", mensaje.getContenido());
            mensajeWs.put("tipoMensaje", "PROPUESTA_PLAN");
            mensajeWs.put("fechaEnvio", mensaje.getFechaEnvio().toString());
            mensajeWs.put("metadata", objectMapper.readValue(mensaje.getMetadata(), Map.class));
            mensajeWs.put("invalidatedMensajeIds", invalidatedIds);

            messagingTemplate.convertAndSend("/topic/conversacion/" + conversacion.getId(), mensajeWs);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("Error al parsear metadata para WebSocket: " + e.getMessage());
        }

        response.put("success", true);
        response.put("message", "Contraoferta enviada al usuario");
        response.put("version", nuevaVersion);
        response.put("mensajeId", mensaje.getId());

        return response;
    }

    // ===== MÉTODOS AUXILIARES =====

    private String generarTextoPropuesta(PropuestaDTO propuesta) {
        if (propuesta.getMensaje() != null && !propuesta.getMensaje().isEmpty()) {
            return propuesta.getMensaje();
        }
        return "Propuesta de plan personalizado";
    }

    private String generarTextoPropuestaOriginal(PropuestaDTO propuesta) {
        StringBuilder sb = new StringBuilder();
        sb.append("**PROPUESTA DE ENTRENAMIENTO**\n\n");
        sb.append("Precio: $").append(propuesta.getPrecio()).append(" COP\n");
        sb.append("Duración: ").append(propuesta.getDuracionDias()).append(" días\n\n");
        sb.append("**Servicios incluidos:**\n");

        if (propuesta.getRutinasMes() != null && propuesta.getRutinasMes() > 0) {
            sb.append("• ").append(propuesta.getRutinasMes()).append(" rutinas personalizadas/mes\n");
        }
        if (propuesta.isSeguimientoSemanal()) {
            sb.append("• Seguimiento semanal\n");
        }
        if (propuesta.getVideollamadasMes() != null && propuesta.getVideollamadasMes() > 0) {
            sb.append("• ").append(propuesta.getVideollamadasMes()).append(" videollamadas/mes\n");
        }
        if (propuesta.isPlanNutricional()) {
            sb.append("• Plan nutricional personalizado\n");
        }
        if (propuesta.isChatDirecto()) {
            sb.append("• Chat directo ilimitado\n");
        }

        if (propuesta.getMensaje() != null && !propuesta.getMensaje().isEmpty()) {
            sb.append("\n📝 **Mensaje:**\n").append(propuesta.getMensaje());
        }

        return sb.toString();
    }

    private String crearJsonServicios(PropuestaDTO propuesta) {
        try {
            Map<String, Object> servicios = new HashMap<>();
            servicios.put("rutinas_mes", propuesta.getRutinasMes());
            servicios.put("seguimiento_semanal", propuesta.isSeguimientoSemanal());
            servicios.put("videollamadas_mes", propuesta.getVideollamadasMes());
            servicios.put("plan_nutricional", propuesta.isPlanNutricional());
            servicios.put("chat_directo", propuesta.isChatDirecto());
            servicios.put("servicios_adicionales", propuesta.getServiciosAdicionales());
            return objectMapper.writeValueAsString(servicios);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String crearMetadataPropuesta(Long contratacionId, Long historialId, int version, PropuestaDTO propuesta,
            String linkPago) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("contratacionId", contratacionId);
            metadata.put("historialId", historialId);
            metadata.put("version", version);
            metadata.put("nombrePlan", "Plan Personalizado");
            metadata.put("precioFinal", propuesta.getPrecio());
            metadata.put("duracionDias", propuesta.getDuracionDias());
            metadata.put("rutinasMes", propuesta.getRutinasMes());
            metadata.put("seguimientoSemanal", propuesta.isSeguimientoSemanal());
            metadata.put("videollamadasMes", propuesta.getVideollamadasMes());
            metadata.put("planNutricional", propuesta.isPlanNutricional());
            metadata.put("mensaje", propuesta.getMensaje());
            if (linkPago != null && !linkPago.isEmpty()) {
                metadata.put("linkPago", linkPago);
            }
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * Obtener datos de pago de una contratación
     */
    public Map<String, Object> obtenerDatosPago(Long contratacionId) {
        HistorialNegociacion historial = historialRepo
                .findTopByContratacionIdAndEstadoPropuestaOrderByVersionDesc(
                        contratacionId,
                        HistorialNegociacion.EstadoPropuesta.ACEPTADA);

        if (historial != null) {
            // Buscar el mensaje de propuesta asociado
            List<Conversacion> conversaciones = conversacionRepo
                    .findByUsuarioIdAndEntrenadorIdOrderByFechaUltimoMensajeDesc(
                            contratacionRepo.findById(contratacionId).orElseThrow().getUsuarioId(),
                            contratacionRepo.findById(contratacionId).orElseThrow().getEntrenadorId());
            Conversacion conversacion = conversaciones.isEmpty() ? null : conversaciones.get(0);
            if (conversacion == null)
                throw new RuntimeException("Conversación no encontrada");
            List<Mensaje> mensajes = mensajeRepo.findByConversacionIdOrderByFechaEnvioAsc(conversacion.getId());

            for (Mensaje mensaje : mensajes) {
                if (mensaje.getTipoMensaje() == Mensaje.TipoMensaje.PROPUESTA_PLAN && mensaje.getMetadata() != null) {
                    try {
                        Map<String, Object> metadata = objectMapper.readValue(mensaje.getMetadata(), Map.class);
                        if (metadata.containsKey("contratacionId") &&
                                ((Number) metadata.get("contratacionId")).longValue() == contratacionId) {
                            return metadata;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Generar nuevo link de pago de MercadoPago
     */
    public String generarNuevoLinkPago(Long contratacionId) throws Exception {
        ContratacionEntrenador contratacion = contratacionRepo.findById(contratacionId)
                .orElseThrow(() -> new RuntimeException("Contratación no encontrada"));

        HistorialNegociacion historial = historialRepo
                .findTopByContratacionIdOrderByVersionDesc(contratacionId);

        if (historial == null) {
            throw new RuntimeException("No se encontró historial de negociación");
        }

        // COMENTADO - Ya no usamos SDK, ahora Bricks maneja el pago
        // String linkPago = mercadoPagoService.crearPreferenciaPago(
        // contratacion.getId(),
        // historial.getPrecioPropuesto(),
        // "Plan de Entrenamiento - " + historial.getDuracionPropuesta() + " días",
        // contratacion.getUsuarioId().toString());
        String linkPago = "#pagar"; // El pago se hace con Bricks en el frontend

        return linkPago;
    }
}
