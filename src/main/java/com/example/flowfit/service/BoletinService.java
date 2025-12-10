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
     * Envía un boletín de forma asíncrona usando BCC (CCO)
     * OPTIMIZACIÓN: En lugar de enviar correos uno por uno en un bucle for,
     * envía un solo correo con todos los destinatarios en BCC (copia oculta)
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
        System.out.println("⚡ Modo: ENVÍO MASIVO CON BCC (optimizado - sin bucle for)");
        
        // Verificar si hay variables personalizadas en el contenido
        boolean tieneVariables = boletin.getContenido().contains("{{");
        
        int exitosos = 0;
        int fallidos = 0;
        
        if (tieneVariables) {
            // Si tiene variables, enviar de forma personalizada (un email por usuario)
            System.out.println("⚠️ Contenido con variables detectado. Enviando individualmente...");
            exitosos = enviarBoletinPersonalizado(boletin, destinatarios);
            fallidos = destinatarios.size() - exitosos;
        } else {
            // Contenido estático: ENVÍO MASIVO CON BCC
            System.out.println("✅ Contenido estático. Usando envío masivo con BCC...");
            
            // Extraer todos los correos
            String[] emailsArray = destinatarios.stream()
                .map(Usuario::getCorreo)
                .filter(email -> email != null && !email.isEmpty())
                .toArray(String[]::new);
            
            try {
                // ENVÍO MASIVO EN UNA SOLA OPERACIÓN CON BCC
                boolean enviado = emailService.enviarCorreoMasivoBCC(
                    boletin.getAsunto(),
                    boletin.getContenido(),
                    emailsArray
                );
                
                if (enviado) {
                    exitosos = emailsArray.length;
                    System.out.println("✅ Email masivo enviado exitosamente a " + exitosos + " destinatarios");
                } else {
                    fallidos = emailsArray.length;
                    System.out.println("❌ Error en envío masivo");
                }
                
            } catch (Exception e) {
                fallidos = emailsArray.length;
                System.err.println("❌ Excepción en envío masivo: " + e.getMessage());
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
     * Envía boletín personalizado (con variables) de forma individual
     * Solo se usa cuando el contenido tiene variables como {{nombre}}
     */
    private int enviarBoletinPersonalizado(BoletinInformativo boletin, List<Usuario> destinatarios) {
        int exitosos = 0;
        
        for (Usuario usuario : destinatarios) {
            try {
                String perfilStr = (usuario.getPerfilUsuario() != null) ? usuario.getPerfilUsuario().name() : "";
                String contenidoPersonalizado = boletin.getContenido()
                    .replace("{{nombre}}", usuario.getNombre() != null ? usuario.getNombre() : "")
                    .replace("{{correo}}", usuario.getCorreo() != null ? usuario.getCorreo() : "")
                    .replace("{{perfil}}", perfilStr);
                
                boolean enviado = emailService.enviarCorreoBoletin(
                    usuario.getCorreo(),
                    usuario.getNombre(),
                    boletin.getAsunto(),
                    contenidoPersonalizado
                );
                
                if (enviado) {
                    exitosos++;
                }
                
                // Pausa para evitar ser bloqueado por el servidor SMTP
                Thread.sleep(200);
                
            } catch (Exception e) {
                System.err.println("❌ Error al enviar a " + usuario.getNombre());
            }
        }
        
        return exitosos;
    }
    
    /**
     * Elimina un boletín
     */
    @Transactional
    public void eliminarBoletin(Long id) {
        boletinRepository.deleteById(id);
    }
}
