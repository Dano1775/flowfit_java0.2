package com.example.flowfit.service;

import com.example.flowfit.model.AsignacionEntrenador;
import com.example.flowfit.model.Usuario;
import com.example.flowfit.repository.AsignacionEntrenadorRepository;
import com.example.flowfit.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AsignacionEntrenadorService {
    
    @Autowired
    private AsignacionEntrenadorRepository asignacionRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    // Obtener todos los entrenadores disponibles
    public List<Usuario> getEntrenadoresDisponibles() {
        return usuarioRepository.findByPerfilUsuarioAndEstado(Usuario.PerfilUsuario.Entrenador, "A");
    }
    
    // Crear solicitud de asignación
    public boolean crearSolicitudAsignacion(Integer usuarioId, Integer entrenadorId, String mensaje) {
        try {
            // Verificar si ya existe una solicitud
            Optional<AsignacionEntrenador> existente = asignacionRepository.findByUsuarioIdAndEntrenadorId(usuarioId, entrenadorId);
            if (existente.isPresent()) {
                return false; // Ya existe una solicitud
            }
            
            // Verificar si el usuario ya tiene un entrenador asignado
            Optional<AsignacionEntrenador> entrenadorActual = asignacionRepository.findEntrenadorActualByUsuarioId(usuarioId);
            if (entrenadorActual.isPresent()) {
                return false; // Ya tiene un entrenador asignado
            }
            
            Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
            Usuario entrenador = usuarioRepository.findById(entrenadorId).orElse(null);
            
            if (usuario == null || entrenador == null) {
                return false;
            }
            
            AsignacionEntrenador asignacion = new AsignacionEntrenador();
            asignacion.setUsuario(usuario);
            asignacion.setEntrenador(entrenador);
            asignacion.setMensajeSolicitud(mensaje);
            asignacion.setEstado(AsignacionEntrenador.EstadoAsignacion.PENDIENTE);
            
            asignacionRepository.save(asignacion);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Obtener solicitudes pendientes de un entrenador
    public List<AsignacionEntrenador> getSolicitudesPendientes(Integer entrenadorId) {
        return asignacionRepository.findByEntrenadorIdAndEstado(entrenadorId, AsignacionEntrenador.EstadoAsignacion.PENDIENTE);
    }
    
    // Aceptar solicitud
    public boolean aceptarSolicitud(Long asignacionId, String mensajeRespuesta) {
        try {
            Optional<AsignacionEntrenador> asignacionOpt = asignacionRepository.findById(asignacionId);
            if (asignacionOpt.isPresent()) {
                AsignacionEntrenador asignacion = asignacionOpt.get();
                asignacion.setEstado(AsignacionEntrenador.EstadoAsignacion.ACEPTADA);
                asignacion.setFechaAceptacion(LocalDateTime.now());
                asignacion.setMensajeRespuesta(mensajeRespuesta);
                asignacionRepository.save(asignacion);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Rechazar solicitud
    public boolean rechazarSolicitud(Long asignacionId, String mensajeRespuesta) {
        try {
            Optional<AsignacionEntrenador> asignacionOpt = asignacionRepository.findById(asignacionId);
            if (asignacionOpt.isPresent()) {
                AsignacionEntrenador asignacion = asignacionOpt.get();
                asignacion.setEstado(AsignacionEntrenador.EstadoAsignacion.RECHAZADA);
                asignacion.setMensajeRespuesta(mensajeRespuesta);
                asignacionRepository.save(asignacion);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Obtener usuarios asignados a un entrenador
    public List<AsignacionEntrenador> getUsuariosAsignados(Integer entrenadorId) {
        return asignacionRepository.findUsuariosAsignadosByEntrenadorId(entrenadorId);
    }
    
    // Obtener usuarios rechazados por un entrenador
    public List<AsignacionEntrenador> getUsuariosRechazados(Integer entrenadorId) {
        return asignacionRepository.findByEntrenadorIdAndEstado(entrenadorId, AsignacionEntrenador.EstadoAsignacion.RECHAZADA);
    }
    
    // Eliminar asignación (permite que el usuario vuelva a solicitar)
    public boolean eliminarAsignacion(Long asignacionId) {
        try {
            Optional<AsignacionEntrenador> asignacionOpt = asignacionRepository.findById(asignacionId);
            if (asignacionOpt.isPresent()) {
                asignacionRepository.delete(asignacionOpt.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Cambiar estado de rechazada a aceptada
    public boolean aceptarUsuarioRechazado(Long asignacionId, String mensajeRespuesta) {
        try {
            Optional<AsignacionEntrenador> asignacionOpt = asignacionRepository.findById(asignacionId);
            if (asignacionOpt.isPresent()) {
                AsignacionEntrenador asignacion = asignacionOpt.get();
                asignacion.setEstado(AsignacionEntrenador.EstadoAsignacion.ACEPTADA);
                asignacion.setFechaAceptacion(LocalDateTime.now());
                asignacion.setMensajeRespuesta(mensajeRespuesta);
                asignacionRepository.save(asignacion);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Obtener entrenador actual de un usuario
    public AsignacionEntrenador getEntrenadorActual(Integer usuarioId) {
        return asignacionRepository.findEntrenadorActualByUsuarioId(usuarioId).orElse(null);
    }
    
    // Obtener historial de solicitudes de un usuario
    public List<AsignacionEntrenador> getHistorialSolicitudes(Integer usuarioId) {
        return asignacionRepository.findByUsuarioId(usuarioId);
    }
}