package com.example.flowfit.service;

import com.example.flowfit.model.PasswordResetToken;
import com.example.flowfit.model.Usuario;
import com.example.flowfit.repository.PasswordResetTokenRepository;
import com.example.flowfit.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Genera un token seguro aleatorio
     */
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /**
     * Crea una solicitud de restablecimiento de contraseña
     * @param correo Email del usuario
     * @return true si se envió el correo correctamente
     */
    @Transactional
    public boolean crearSolicitudReset(String correo) {
        System.out.println("\n🔐 SOLICITUD DE RESET DE CONTRASEÑA");
        System.out.println("📧 Correo: " + correo);
        
        // Buscar usuario por correo
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
        
        if (usuarioOpt.isEmpty()) {
            System.out.println("❌ Usuario no encontrado");
            // Por seguridad, no revelamos si el email existe o no
            return true;
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Verificar que la cuenta esté activa
        if (!"A".equals(usuario.getEstado())) {
            System.out.println("❌ Cuenta no activa. Estado: " + usuario.getEstado());
            return false;
        }
        
        System.out.println("✅ Usuario encontrado: " + usuario.getNombre());
        
        // Eliminar tokens anteriores del usuario
        tokenRepository.deleteByUsuario(usuario);
        System.out.println("🗑️ Tokens anteriores eliminados");
        
        // Generar nuevo token
        String tokenValue = generateSecureToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenValue);
        token.setUsuario(usuario);
        token.setUsado(false);
        
        tokenRepository.save(token);
        System.out.println("💾 Token generado y guardado: " + tokenValue.substring(0, 10) + "...");
        System.out.println("⏰ Válido hasta: " + token.getFechaExpiracion());
        
        // Enviar correo con el enlace
        boolean emailEnviado = emailService.enviarCorreoResetPassword(
            usuario.getCorreo(),
            usuario.getNombre(),
            tokenValue
        );
        
        if (emailEnviado) {
            System.out.println("✅ Correo de recuperación enviado correctamente");
        } else {
            System.out.println("❌ Error al enviar correo");
        }
        
        return emailEnviado;
    }
    
    /**
     * Valida un token de reset
     * @param token Token a validar
     * @return Optional con el token si es válido
     */
    public Optional<PasswordResetToken> validarToken(String token) {
        System.out.println("\n🔍 VALIDANDO TOKEN");
        System.out.println("Token recibido: " + token.substring(0, Math.min(10, token.length())) + "...");
        
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            System.out.println("❌ Token no encontrado en BD");
            return Optional.empty();
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        
        if (resetToken.isUsado()) {
            System.out.println("❌ Token ya fue usado");
            return Optional.empty();
        }
        
        if (resetToken.isExpired()) {
            System.out.println("❌ Token expirado");
            return Optional.empty();
        }
        
        System.out.println("✅ Token válido para usuario: " + resetToken.getUsuario().getNombre());
        return tokenOpt;
    }
    
    /**
     * Cambia la contraseña del usuario usando el token
     * @param token Token de reset
     * @param nuevaClave Nueva contraseña
     * @return true si se cambió correctamente
     */
    @Transactional
    public boolean cambiarPassword(String token, String nuevaClave) {
        System.out.println("\n🔄 CAMBIO DE CONTRASEÑA");
        
        Optional<PasswordResetToken> tokenOpt = validarToken(token);
        
        if (tokenOpt.isEmpty()) {
            System.out.println("❌ Token inválido");
            return false;
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        Usuario usuario = resetToken.getUsuario();
        
        // Encriptar nueva contraseña
        String claveEncriptada = passwordEncoder.encode(nuevaClave);
        usuario.setClave(claveEncriptada);
        
        usuarioRepository.save(usuario);
        System.out.println("✅ Contraseña actualizada para: " + usuario.getNombre());
        
        // Marcar token como usado
        resetToken.setUsado(true);
        tokenRepository.save(resetToken);
        System.out.println("🔒 Token marcado como usado");
        
        // Eliminar todos los demás tokens del usuario
        tokenRepository.deleteByUsuario(usuario);
        System.out.println("🗑️ Otros tokens del usuario eliminados");
        
        return true;
    }
    
    /**
     * Limpia tokens expirados (se puede ejecutar periódicamente)
     */
    @Transactional
    public void limpiarTokensExpirados() {
        tokenRepository.deleteByFechaExpiracionBefore(LocalDateTime.now());
        System.out.println("🧹 Tokens expirados eliminados");
    }
}
