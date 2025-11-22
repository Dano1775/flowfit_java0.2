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
                    .filter(u -> "Usuario".equals(u.getPerfilUsuario()))
                    .collect(Collectors.toList());
                
            case ENTRENADORES:
                return todos.stream()
                    .filter(u -> "Entrenador".equals(u.getPerfilUsuario()))
                    .collect(Collectors.toList());
                
            case NUTRICIONISTAS:
                return todos.stream()
                    .filter(u -> "Nutricionista".equals(u.getPerfilUsuario()))
                    .collect(Collectors.toList());
                
            case ADMINISTRADORES:
                return todos.stream()
                    .filter(u -> "Administrador".equals(u.getPerfilUsuario()))
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
        
        System.out.println("👥 Total de destinatarios: " + destinatarios.size());
        System.out.println("📝 Tipo: " + boletin.getTipoDestinatario().getDescripcion());
        
        int exitosos = 0;
        int fallidos = 0;
        
        // Enviar correo a cada destinatario
        for (Usuario usuario : destinatarios) {
            try {
                // Reemplazar placeholders en el contenido
                String contenidoPersonalizado = boletin.getContenido()
                    .replace("{{nombre}}", usuario.getNombre())
                    .replace("{{correo}}", usuario.getCorreo())
                    .replace("{{perfil}}", usuario.getPerfilUsuario());
                
                boolean enviado = emailService.enviarCorreoBoletin(
                    usuario.getCorreo(),
                    usuario.getNombre(),
                    boletin.getAsunto(),
                    contenidoPersonalizado
                );
                
                if (enviado) {
                    exitosos++;
                    System.out.println("✅ Enviado a: " + usuario.getNombre() + " (" + usuario.getCorreo() + ")");
                } else {
                    fallidos++;
                    System.out.println("❌ Error al enviar a: " + usuario.getNombre());
                }
                
                // Pequeña pausa para no saturar el servidor SMTP
                Thread.sleep(500);
                
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
    }
    
    /**
     * Elimina un boletín
     */
    @Transactional
    public void eliminarBoletin(Long id) {
        boletinRepository.deleteById(id);
    }
}
