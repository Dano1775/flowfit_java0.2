package com.example.flowfit.service;

import com.example.flowfit.model.BoletinInformativo;
import com.example.flowfit.model.BoletinInformativo.TipoDestinatario;
import com.example.flowfit.model.BoletinInformativo.EstadoEnvio;
import com.example.flowfit.model.Usuario;
import com.example.flowfit.repository.BoletinInformativoRepository;
import com.example.flowfit.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de boletines informativos y envíos masivos
 */
@Service
public class BoletinService {
    
    @Autowired
    private BoletinInformativoRepository boletinRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Crea un nuevo boletín
     */
    @Transactional
    public BoletinInformativo crearBoletin(String asunto, String contenido, 
                                           TipoDestinatario tipoDestinatario, 
                                           String creadoPor) {
        BoletinInformativo boletin = new BoletinInformativo();
        boletin.setAsunto(asunto);
        boletin.setContenido(contenido);
        boletin.setTipoDestinatario(tipoDestinatario);
        boletin.setCreadoPor(creadoPor);
        boletin.setEstadoEnvio(EstadoEnvio.PENDIENTE);
        
        return boletinRepository.save(boletin);
    }
    
    /**
     * Obtiene todos los boletines ordenados por fecha
     */
    public List<BoletinInformativo> obtenerTodosBoletines() {
        return boletinRepository.findAllByOrderByFechaCreacionDesc();
    }
    
    /**
     * Obtiene un boletín por ID
     */
    public BoletinInformativo obtenerBoletinPorId(Long id) {
        return boletinRepository.findById(id).orElse(null);
    }
    
    /**
     * Obtiene destinatarios según el tipo seleccionado
     */
    private List<Usuario> obtenerDestinatarios(TipoDestinatario tipo) {
        List<Usuario> todos = usuarioRepository.findAll();
        
        switch (tipo) {
            case TODOS:
                return todos;
                
            case USUARIOS:
                return todos.stream()
                    .filter(u -> u.getPerfilUsuario() != null && "Usuario".equals(u.getPerfilUsuario().name()))
                    .collect(Collectors.toList());
                
            case ENTRENADORES:
                return todos.stream()
                    .filter(u -> u.getPerfilUsuario() != null && "Entrenador".equals(u.getPerfilUsuario().name()))
                    .collect(Collectors.toList());
                
            case NUTRICIONISTAS:
                return todos.stream()
                    .filter(u -> u.getPerfilUsuario() != null && "Nutricionista".equals(u.getPerfilUsuario().name()))
                    .collect(Collectors.toList());
                
            case ADMINISTRADORES:
                return todos.stream()
                    .filter(u -> u.getPerfilUsuario() != null && "Administrador".equals(u.getPerfilUsuario().name()))
                    .collect(Collectors.toList());
                
            case USUARIOS_ACTIVOS:
                return todos.stream()
                    .filter(u -> "A".equals(u.getEstado()))
                    .collect(Collectors.toList());
                
            case USUARIOS_INACTIVOS:
                return todos.stream()
                    .filter(u -> "I".equals(u.getEstado()))
                    .collect(Collectors.toList());
                
            default:
                return todos;
        }
    }
    
    /**
     * Envía un boletín de forma asíncrona
     */
    @Async
    @Transactional
    public void enviarBoletin(Long boletinId) {
        System.out.println("\n📧 INICIANDO ENVÍO MASIVO DE BOLETÍN #" + boletinId);
        
        BoletinInformativo boletin = boletinRepository.findById(boletinId)
            .orElseThrow(() -> new RuntimeException("Boletín no encontrado"));
        
        // Cambiar estado a ENVIANDO
        boletin.setEstadoEnvio(EstadoEnvio.ENVIANDO);
        boletinRepository.save(boletin);
        
        // Obtener destinatarios
        List<Usuario> destinatarios = obtenerDestinatarios(boletin.getTipoDestinatario());
        boletin.setTotalDestinatarios(destinatarios.size());
        boletinRepository.save(boletin);
        
        // Detectar si el contenido tiene variables para optimizar el envío
        boolean tieneVariables = boletin.getContenido().contains("{{");
        boolean esContenidoEstatico = !tieneVariables;
        
        // Configurar delay según tipo de contenido
        int delayMs = esContenidoEstatico ? 100 : 500; // Templates estáticos: 100ms, Personalizados: 500ms
        
        System.out.println("👥 Total de destinatarios: " + destinatarios.size());
        System.out.println("📝 Tipo: " + boletin.getTipoDestinatario().getDescripcion());
        System.out.println("⚡ Modo: " + (esContenidoEstatico ? "ESTÁTICO (optimizado)" : "PERSONALIZADO (con variables)"));
        System.out.println("⏱️ Delay entre envíos: " + delayMs + "ms");
        
        int exitosos = 0;
        int fallidos = 0;
        
        // Para contenido estático, preparar el HTML una sola vez
        String contenidoFinal = boletin.getContenido();
        
        // Enviar correo a cada destinatario
        for (Usuario usuario : destinatarios) {
            try {
                // Si tiene variables, reemplazar por cada usuario
                if (tieneVariables) {
                    String perfilStr = (usuario.getPerfilUsuario() != null) ? usuario.getPerfilUsuario().name() : "";
                    contenidoFinal = boletin.getContenido()
                        .replace("{{nombre}}", usuario.getNombre() != null ? usuario.getNombre() : "")
                        .replace("{{correo}}", usuario.getCorreo() != null ? usuario.getCorreo() : "")
                        .replace("{{perfil}}", perfilStr);
                }
                // Si es estático, usar el contenido original sin modificaciones
                
                boolean enviado = emailService.enviarCorreoBoletin(
                    usuario.getCorreo(),
                    usuario.getNombre(),
                    boletin.getAsunto(),
                    contenidoFinal
                );
                
                if (enviado) {
                    exitosos++;
                    if (exitosos % 50 == 0) { // Log cada 50 envíos para no saturar consola
                        System.out.println("✅ Progreso: " + exitosos + "/" + destinatarios.size() + " enviados");
                    }
                } else {
                    fallidos++;
                    System.out.println("❌ Error al enviar a: " + usuario.getNombre());
                }
                
                // Pausa optimizada: más corta para contenido estático
                Thread.sleep(delayMs);
                
            } catch (Exception e) {
                fallidos++;
                System.out.println("❌ Excepción al enviar a " + usuario.getNombre() + ": " + e.getMessage());
            }
            
            // Actualizar progreso cada 10 envíos
            if ((exitosos + fallidos) % 10 == 0) {
                boletin.setEnviadosExitosos(exitosos);
                boletin.setEnviadosFallidos(fallidos);
                boletinRepository.save(boletin);
            }
        }
        
        // Actualizar estado final
        boletin.setEnviadosExitosos(exitosos);
        boletin.setEnviadosFallidos(fallidos);
        boletin.setFechaEnvio(LocalDateTime.now());
        
        if (fallidos == 0) {
            boletin.setEstadoEnvio(EstadoEnvio.COMPLETADO);
        } else if (exitosos == 0) {
            boletin.setEstadoEnvio(EstadoEnvio.FALLIDO);
        } else {
            boletin.setEstadoEnvio(EstadoEnvio.COMPLETADO); // Parcialmente exitoso
        }
        
        boletinRepository.save(boletin);
        
        System.out.println("\n✅ ENVÍO MASIVO COMPLETADO");
        System.out.println("📊 Exitosos: " + exitosos + " | Fallidos: " + fallidos);
        System.out.println("⏱️ Tiempo aproximado: " + ((exitosos + fallidos) * delayMs / 1000) + " segundos");
    }
    
    /**
     * Elimina un boletín
     */
    @Transactional
    public void eliminarBoletin(Long id) {
        boletinRepository.deleteById(id);
    }
}
