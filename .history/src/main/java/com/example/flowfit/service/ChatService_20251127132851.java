package com.example.flowfit.service;

import com.example.flowfit.model.*;
import com.example.flowfit.repository.*;
import com.example.flowfit.dto.MensajeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ChatService {
    
    @Autowired
    private ConversacionRepository conversacionRepo;
    
    @Autowired
    private MensajeRepository mensajeRepo;
    
    @Autowired
    private UsuarioRepository usuarioRepo;
    
    /**
     * Obtener o crear conversación entre usuario y entrenador
     */
    @Transactional
    public Conversacion obtenerOCrearConversacion(Integer usuarioId, Integer entrenadorId) {
        Optional<Conversacion> existente = conversacionRepo.findByUsuarioIdAndEntrenadorId(usuarioId, entrenadorId);
        
        if (existente.isPresent()) {
            return existente.get();
        }
        
        // Crear nueva conversación
        Conversacion nueva = new Conversacion();
        nueva.setUsuarioId(usuarioId);
        nueva.setEntrenadorId(entrenadorId);
        nueva.setEstado(Conversacion.EstadoConversacion.ACTIVA);
        
        return conversacionRepo.save(nueva);
    }
    
    /**
     * Enviar mensaje de texto normal
     */
    @Transactional
    public Mensaje enviarMensaje(MensajeDTO mensajeDTO, Integer remitenteId) {
        Mensaje mensaje = new Mensaje();
        mensaje.setConversacionId(mensajeDTO.getConversacionId());
        mensaje.setRemitenteId(remitenteId);
        mensaje.setContenido(mensajeDTO.getContenido());
        mensaje.setTipoMensaje(Mensaje.TipoMensaje.TEXTO);
        
        return mensajeRepo.save(mensaje);
    }
    
    /**
     * Obtener todos los mensajes de una conversación
     */
    public List<Mensaje> obtenerMensajes(Long conversacionId) {
        return mensajeRepo.findByConversacionIdOrderByFechaEnvioAsc(conversacionId);
    }
    
    /**
     * Marcar mensajes como leídos
     */
    @Transactional
    public void marcarComoLeidos(Long conversacionId, Integer usuarioId) {
        mensajeRepo.marcarComoLeidos(conversacionId, usuarioId);
        
        // Actualizar contador de no leídos
        Conversacion conversacion = conversacionRepo.findById(conversacionId).orElseThrow();
        if (conversacion.getUsuarioId().equals(usuarioId)) {
            conversacion.setMensajesNoLeidosUsuario(0);
        } else if (conversacion.getEntrenadorId().equals(usuarioId)) {
            conversacion.setMensajesNoLeidosEntrenador(0);
        }
        conversacionRepo.save(conversacion);
    }
    
    /**
     * Obtener conversaciones de un usuario
     */
    public List<Conversacion> obtenerConversacionesUsuario(Integer usuarioId) {
        return conversacionRepo.findByUsuarioIdOrderByFechaUltimoMensajeDesc(usuarioId);
    }
    
    /**
     * Obtener conversaciones de un entrenador
     */
    public List<Conversacion> obtenerConversacionesEntrenador(Integer entrenadorId) {
        return conversacionRepo.findByEntrenadorIdOrderByFechaUltimoMensajeDesc(entrenadorId);
    }
    
    /**
     * Contar mensajes no leídos
     */
    public Long contarMensajesNoLeidos(Integer personaId, boolean esEntrenador) {
        if (esEntrenador) {
            return conversacionRepo.contarMensajesNoLeidosEntrenador(personaId);
        } else {
            return conversacionRepo.contarMensajesNoLeidosUsuario(personaId);
        }
    }
}
