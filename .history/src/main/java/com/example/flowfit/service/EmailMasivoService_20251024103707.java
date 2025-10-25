package com.example.flowfit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.example.flowfit.repository.UsuarioRepository;
import com.example.flowfit.model.Usuario;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio para el envío masivo de correos electrónicos
 */
@Service
public class EmailMasivoService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Value("${flowfit.email.from:noreply@flowfit.com}")
    private String fromEmail;
    
    @Value("${flowfit.email.fromName:FlowFit}")
    private String fromName;
    
    /**
     * Envía un correo masivo a todos los usuarios activos
     */
    @Async
    public CompletableFuture<ResultadoEnvio> enviarCorreoMasivo(String asunto, String mensaje, String tipoDestinatario) {
        ResultadoEnvio resultado = new ResultadoEnvio();
        
        try {
            List<Usuario> destinatarios = obtenerDestinatarios(tipoDestinatario);
            resultado.setTotalDestinatarios(destinatarios.size());
            
            for (Usuario usuario : destinatarios) {
                try {
                    enviarCorreoIndividual(usuario.getCorreo(), asunto, mensaje, usuario.getNombre());
                    resultado.incrementarExitosos();
                } catch (Exception e) {
                    resultado.incrementarFallidos();
                    System.err.println("Error enviando correo a " + usuario.getCorreo() + ": " + e.getMessage());
                }
                
                // Pausa pequeña entre envíos para no saturar el servidor SMTP
                Thread.sleep(100);
            }
            
        } catch (Exception e) {
            resultado.setError("Error general en el envío: " + e.getMessage());
        }
        
        return CompletableFuture.completedFuture(resultado);
    }
    
    /**
     * Envía un correo individual
     */
    private void enviarCorreoIndividual(String correoDestinatario, String asunto, String mensaje, String nombreDestinatario) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(correoDestinatario);
        mailMessage.setSubject(asunto);
        
        // Personalizar el mensaje con el nombre del usuario
        String mensajePersonalizado = mensaje.replace("{nombre}", nombreDestinatario);
        mensajePersonalizado = mensajePersonalizado.replace("{email}", correoDestinatario);
        
        mailMessage.setText(mensajePersonalizado);
        
        mailSender.send(mailMessage);
    }
    
    /**
     * Obtiene la lista de destinatarios según el tipo seleccionado
     */
    private List<Usuario> obtenerDestinatarios(String tipoDestinatario) {
        switch (tipoDestinatario.toUpperCase()) {
            case "TODOS":
                return usuarioRepository.findByEstado("A");
            case "ENTRENADORES":
                return usuarioRepository.findByPerfilUsuarioStringAndEstado("Entrenador", "A");
            case "USUARIOS":
            case "CLIENTES":
                return usuarioRepository.findByPerfilUsuarioStringAndEstado("Usuario", "A");
            case "NUTRICIONISTAS":
                return usuarioRepository.findByPerfilUsuarioStringAndEstado("Nutricionista", "A");
            case "ADMINISTRADORES":
                return usuarioRepository.findByPerfilUsuarioStringAndEstado("Administrador", "A");
            default:
                return usuarioRepository.findByEstado("A");
        }
    }
    
    /**
     * Clase interna para almacenar el resultado del envío
     */
    public static class ResultadoEnvio {
        private int totalDestinatarios = 0;
        private int enviadosExitosos = 0;
        private int enviadosFallidos = 0;
        private String error = null;
        
        public void incrementarExitosos() {
            this.enviadosExitosos++;
        }
        
        public void incrementarFallidos() {
            this.enviadosFallidos++;
        }
        
        // Getters y setters
        public int getTotalDestinatarios() { return totalDestinatarios; }
        public void setTotalDestinatarios(int totalDestinatarios) { this.totalDestinatarios = totalDestinatarios; }
        
        public int getEnviadosExitosos() { return enviadosExitosos; }
        public void setEnviadosExitosos(int enviadosExitosos) { this.enviadosExitosos = enviadosExitosos; }
        
        public int getEnviadosFallidos() { return enviadosFallidos; }
        public void setEnviadosFallidos(int enviadosFallidos) { this.enviadosFallidos = enviadosFallidos; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public double getPorcentajeExito() {
            if (totalDestinatarios == 0) return 0;
            return (double) enviadosExitosos / totalDestinatarios * 100;
        }
    }
}