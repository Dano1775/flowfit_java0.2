package com.example.flowfit.service;

import com.example.flowfit.dto.PropuestaDTO;
import com.example.flowfit.model.*;
import com.example.flowfit.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Entrenador envía propuesta inicial al usuario
     */
    @Transactional
    public Map<String, Object> enviarPropuestaInicial(Long conversacionId, Integer entrenadorId, PropuestaDTO propuesta) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Conversacion conversacion = conversacionRepo.findById(conversacionId)
                .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));
            
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
            
            // Crear historial de negociación
            HistorialNegociacion historial = new HistorialNegociacion();
            historial.setContratacionId(contratacion.getId());
            historial.setVersion(1);
            historial.setPropuestoPor(HistorialNegociacion.PropuestoPor.ENTRENADOR);
            historial.setPrecioPropuesto(propuesta.getPrecio());
            historial.setDuracionPropuesta(propuesta.getDuracionDias());
            historial.setServiciosPropuestos(crearJsonServicios(propuesta));
            historial.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.PENDIENTE);
            historial.setMensaje(propuesta.getMensaje());
            
            historialRepo.save(historial);
            
            // Crear preferencia de pago MercadoPago
            String linkPago = mercadoPagoService.crearPreferenciaPago(
                contratacion.getId(), propuesta.getPrecio(), "Plan personalizado FlowFit", "Propuesta de plan enviada por entrenador");
            
            // Crear mensaje especial en el chat
            Mensaje mensaje = new Mensaje();
            mensaje.setConversacionId(conversacionId);
            mensaje.setRemitenteId(entrenadorId);
            mensaje.setTipoMensaje(Mensaje.TipoMensaje.PROPUESTA_PLAN);
            mensaje.setContenido(generarTextoPropuesta(propuesta));
            mensaje.setMetadata(crearMetadataPropuesta(contratacion.getId(), historial.getId(), true, propuesta, linkPago));
            
            mensajeRepo.save(mensaje);
            
            response.put("success", true);
            response.put("message", "Propuesta enviada correctamente");
            response.put("contratacionId", contratacion.getId());
            response.put("mensajeId", mensaje.getId());
            response.put("linkPago", linkPago);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        return response;
    }
    
    /**
     * Usuario responde a una propuesta (ACEPTAR, RECHAZAR, CONTRAOFERTA)
     */
    @Transactional
    public Map<String, Object> responderPropuesta(Long contratacionId, Integer usuarioId, String accion, PropuestaDTO contraoferta) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ContratacionEntrenador contratacion = contratacionRepo.findById(contratacionId)
                .orElseThrow(() -> new RuntimeException("Contratación no encontrada"));
            
            // Validar que sea el usuario correcto
            if (!contratacion.getUsuarioId().equals(usuarioId)) {
                throw new RuntimeException("No tienes permiso para responder esta propuesta");
            }
            
            switch (accion.toUpperCase()) {
                case "ACEPTAR":
                    return aceptarPropuesta(contratacion, usuarioId);
                    
                case "RECHAZAR":
                    return rechazarPropuesta(contratacion, usuarioId, contraoferta != null ? contraoferta.getMensaje() : null);
                    
                case "CONTRAOFERTA":
                    return enviarContraoferta(contratacion, usuarioId, contraoferta);
                    
                default:
                    throw new RuntimeException("Acción no válida");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            e.printStackTrace();
            return response;
        }
    }
    
    /**
     * Entrenador responde a una contraoferta del usuario
     */
    @Transactional
    public Map<String, Object> entrenadorRespondeContraoferta(Long contratacionId, Integer entrenadorId, String accion, PropuestaDTO nuevaPropuesta) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ContratacionEntrenador contratacion = contratacionRepo.findById(contratacionId)
                .orElseThrow(() -> new RuntimeException("Contratación no encontrada"));
            
            if (!contratacion.getEntrenadorId().equals(entrenadorId)) {
                throw new RuntimeException("No tienes permiso para responder esta propuesta");
            }
            
            switch (accion.toUpperCase()) {
                case "ACEPTAR":
                    return entrenadorAceptaContraoferta(contratacion, entrenadorId);
                    
                case "RECHAZAR":
                    return rechazarPropuesta(contratacion, entrenadorId, nuevaPropuesta != null ? nuevaPropuesta.getMensaje() : null);
                    
                case "CONTRAOFERTA":
                    return enviarContraofertaEntrenador(contratacion, entrenadorId, nuevaPropuesta);
                    
                default:
                    throw new RuntimeException("Acción no válida");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            e.printStackTrace();
            return response;
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
        HistorialNegociacion ultimaPropuesta = historialRepo.findTopByContratacionIdOrderByVersionDesc(contratacion.getId());
        if (ultimaPropuesta != null) {
            ultimaPropuesta.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.ACEPTADA);
            ultimaPropuesta.setFechaRespuesta(LocalDateTime.now());
            historialRepo.save(ultimaPropuesta);
        }
        
        // Crear mensaje de aceptación
        Conversacion conversacion = conversacionRepo
            .findByUsuarioIdAndEntrenadorId(contratacion.getUsuarioId(), contratacion.getEntrenadorId())
            .orElseThrow();
        
        Mensaje mensaje = new Mensaje();
        mensaje.setConversacionId(conversacion.getId());
        mensaje.setRemitenteId(usuarioId);
        mensaje.setTipoMensaje(Mensaje.TipoMensaje.ACEPTACION_PROPUESTA);
        mensaje.setContenido("✅ He aceptado tu propuesta. Procederé con el pago.");
        mensaje.setMetadata("{\"contratacionId\": " + contratacion.getId() + "}");
        mensajeRepo.save(mensaje);
        
        response.put("success", true);
        response.put("message", "Propuesta aceptada. Generando link de pago...");
        response.put("contratacionId", contratacion.getId());
        response.put("requierePago", true);
        
        return response;
    }
    
    private Map<String, Object> entrenadorAceptaContraoferta(ContratacionEntrenador contratacion, Integer entrenadorId) {
        Map<String, Object> response = new HashMap<>();
        
        // Cambiar estado a PENDIENTE_PAGO
        contratacion.setEstado(ContratacionEntrenador.EstadoContratacion.PENDIENTE_PAGO);
        contratacion.setFechaAprobacion(LocalDateTime.now());
        contratacionRepo.save(contratacion);
        
        // Actualizar historial
        HistorialNegociacion ultimaPropuesta = historialRepo.findTopByContratacionIdOrderByVersionDesc(contratacion.getId());
        if (ultimaPropuesta != null) {
            ultimaPropuesta.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.ACEPTADA);
            ultimaPropuesta.setFechaRespuesta(LocalDateTime.now());
            historialRepo.save(ultimaPropuesta);
        }
        
        // Crear mensaje de aceptación
        Conversacion conversacion = conversacionRepo
            .findByUsuarioIdAndEntrenadorId(contratacion.getUsuarioId(), contratacion.getEntrenadorId())
            .orElseThrow();
        
        Mensaje mensaje = new Mensaje();
        mensaje.setConversacionId(conversacion.getId());
        mensaje.setRemitenteId(entrenadorId);
        mensaje.setTipoMensaje(Mensaje.TipoMensaje.ACEPTACION_PROPUESTA);
        mensaje.setContenido("✅ He aceptado tu contraoferta. Puedes proceder con el pago.");
        mensaje.setMetadata("{\"contratacionId\": " + contratacion.getId() + "}");
        mensajeRepo.save(mensaje);
        
        response.put("success", true);
        response.put("message", "Contraoferta aceptada. El usuario puede proceder al pago.");
        response.put("contratacionId", contratacion.getId());
        
        return response;
    }
    
    private Map<String, Object> rechazarPropuesta(ContratacionEntrenador contratacion, Integer usuarioId, String motivo) {
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
        HistorialNegociacion ultimaPropuesta = historialRepo.findTopByContratacionIdOrderByVersionDesc(contratacion.getId());
        if (ultimaPropuesta != null) {
            ultimaPropuesta.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.RECHAZADA);
            ultimaPropuesta.setFechaRespuesta(LocalDateTime.now());
            historialRepo.save(ultimaPropuesta);
        }
        
        // Crear mensaje de rechazo
        Conversacion conversacion = conversacionRepo
            .findByUsuarioIdAndEntrenadorId(contratacion.getUsuarioId(), contratacion.getEntrenadorId())
            .orElseThrow();
        
        Mensaje mensaje = new Mensaje();
        mensaje.setConversacionId(conversacion.getId());
        mensaje.setRemitenteId(usuarioId);
        mensaje.setTipoMensaje(Mensaje.TipoMensaje.RECHAZO_PROPUESTA);
        mensaje.setContenido("❌ He decidido no aceptar esta propuesta. " + 
                           (motivo != null && !motivo.isEmpty() ? "\n\nMotivo: " + motivo : ""));
        mensaje.setMetadata("{\"contratacionId\": " + contratacion.getId() + "}");
        mensajeRepo.save(mensaje);
        
        response.put("success", true);
        response.put("message", "Propuesta rechazada. Puedes continuar conversando para ajustar términos.");
        
        return response;
    }
    
    private Map<String, Object> enviarContraoferta(ContratacionEntrenador contratacion, Integer usuarioId, PropuestaDTO contraoferta) {
        Map<String, Object> response = new HashMap<>();
        
        // Validar máximo de versiones
        if (contratacion.getVersionNegociacion() >= 5) {
            throw new RuntimeException(
                "Se alcanzó el límite de contraofertas (5 versiones). " +
                "Acepta la propuesta actual o recházala para terminar la negociación."
            );
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
        HistorialNegociacion propuestaAnterior = historialRepo.findTopByContratacionIdOrderByVersionDesc(contratacion.getId());
        if (propuestaAnterior != null) {
            propuestaAnterior.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.CONTRAOFERTA);
            propuestaAnterior.setFechaRespuesta(LocalDateTime.now());
            historialRepo.save(propuestaAnterior);
        }
        
        // Crear nuevo historial
        HistorialNegociacion nuevoHistorial = new HistorialNegociacion();
        nuevoHistorial.setContratacionId(contratacion.getId());
        nuevoHistorial.setVersion(nuevaVersion);
        nuevoHistorial.setPropuestoPor(HistorialNegociacion.PropuestoPor.USUARIO);
        nuevoHistorial.setPrecioPropuesto(contraoferta.getPrecio());
        nuevoHistorial.setDuracionPropuesta(contraoferta.getDuracionDias());
        nuevoHistorial.setServiciosPropuestos(crearJsonServicios(contraoferta));
        nuevoHistorial.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.PENDIENTE);
        nuevoHistorial.setMensaje(contraoferta.getMensaje());
        historialRepo.save(nuevoHistorial);
        
        // Crear mensaje en chat
        Conversacion conversacion = conversacionRepo
            .findByUsuarioIdAndEntrenadorId(contratacion.getUsuarioId(), contratacion.getEntrenadorId())
            .orElseThrow();
        
        Mensaje mensaje = new Mensaje();
        mensaje.setConversacionId(conversacion.getId());
        mensaje.setRemitenteId(usuarioId);
        mensaje.setTipoMensaje(Mensaje.TipoMensaje.PROPUESTA_PLAN);
        mensaje.setContenido(generarTextoPropuesta(contraoferta));
        mensaje.setMetadata(crearMetadataPropuesta(contratacion.getId(), nuevoHistorial.getId(), true, contraoferta, null));
        mensajeRepo.save(mensaje);
        
        response.put("success", true);
        response.put("message", "Contraoferta enviada al entrenador");
        response.put("version", nuevaVersion);
        response.put("mensajeId", mensaje.getId());
        
        return response;
    }
    
    private Map<String, Object> enviarContraofertaEntrenador(ContratacionEntrenador contratacion, Integer entrenadorId, PropuestaDTO contraoferta) {
        Map<String, Object> response = new HashMap<>();
        
        // Validar máximo de versiones
        if (contratacion.getVersionNegociacion() >= 5) {
            throw new RuntimeException(
                "Se alcanzó el límite de contraofertas (5 versiones). " +
                "Acepta la propuesta actual o recházala para terminar la negociación."
            );
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
        HistorialNegociacion propuestaAnterior = historialRepo.findTopByContratacionIdOrderByVersionDesc(contratacion.getId());
        if (propuestaAnterior != null) {
            propuestaAnterior.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.CONTRAOFERTA);
            propuestaAnterior.setFechaRespuesta(LocalDateTime.now());
            historialRepo.save(propuestaAnterior);
        }
        
        // Crear nuevo historial
        HistorialNegociacion nuevoHistorial = new HistorialNegociacion();
        nuevoHistorial.setContratacionId(contratacion.getId());
        nuevoHistorial.setVersion(nuevaVersion);
        nuevoHistorial.setPropuestoPor(HistorialNegociacion.PropuestoPor.ENTRENADOR);
        nuevoHistorial.setPrecioPropuesto(contraoferta.getPrecio());
        nuevoHistorial.setDuracionPropuesta(contraoferta.getDuracionDias());
        nuevoHistorial.setServiciosPropuestos(crearJsonServicios(contraoferta));
        nuevoHistorial.setEstadoPropuesta(HistorialNegociacion.EstadoPropuesta.PENDIENTE);
        nuevoHistorial.setMensaje(contraoferta.getMensaje());
        historialRepo.save(nuevoHistorial);
        
        // Crear mensaje en chat
        Conversacion conversacion = conversacionRepo
            .findByUsuarioIdAndEntrenadorId(contratacion.getUsuarioId(), contratacion.getEntrenadorId())
            .orElseThrow();
        
        Mensaje mensaje = new Mensaje();
        mensaje.setConversacionId(conversacion.getId());
        mensaje.setRemitenteId(entrenadorId);
        mensaje.setTipoMensaje(Mensaje.TipoMensaje.PROPUESTA_PLAN);
        mensaje.setContenido(generarTextoPropuesta(contraoferta));
        mensaje.setMetadata(crearMetadataPropuesta(contratacion.getId(), nuevoHistorial.getId(), true, contraoferta));
        mensajeRepo.save(mensaje);
        
        response.put("success", true);
        response.put("message", "Contraoferta enviada al usuario");
        response.put("version", nuevaVersion);
        response.put("mensajeId", mensaje.getId());
        
        return response;
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    private String generarTextoPropuesta(PropuestaDTO propuesta) {
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
    
    private String crearMetadataPropuesta(Long contratacionId, Long historialId, boolean requiereRespuesta, PropuestaDTO propuesta, String linkPago) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("contratacionId", contratacionId);
            metadata.put("historialId", historialId);
            metadata.put("requiereRespuesta", requiereRespuesta);
            metadata.put("precio", propuesta.getPrecio());
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
}
