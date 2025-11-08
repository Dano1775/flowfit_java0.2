package com.example.flowfit.controller;

import com.example.flowfit.model.PasswordResetToken;
import com.example.flowfit.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * API REST para gestión de recuperación de contraseñas
 * Endpoints consumidos por el frontend de login y el sitio de InfinityFree
 */
@RestController
@RequestMapping("/api/password-reset")
@CrossOrigin(origins = "*") // Permitir acceso desde InfinityFree
public class PasswordResetController {
    
    @Autowired
    private PasswordResetService resetService;
    
    /**
     * POST /api/password-reset/request
     * Solicita un reset de contraseña
     * Body: { "email": "usuario@example.com" }
     */
    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> solicitarReset(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String email = request.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "El correo electrónico es requerido");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean enviado = resetService.crearSolicitudReset(email);
        
        if (enviado) {
            response.put("success", true);
            response.put("message", "Si el correo existe, recibirás instrucciones para restablecer tu contraseña.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Ocurrió un error. Por favor, intenta nuevamente.");
            return ResponseEntity.status(500).body(response);
        }
            }
    
    /**
     * GET /api/password-reset/validate/{token}
     * Valida si un token es válido
     * Usado por InfinityFree al cargar la página de reset
     */
    @GetMapping("/validate/{token}")
    public ResponseEntity<Map<String, Object>> validarToken(@PathVariable String token) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<PasswordResetToken> tokenOpt = resetService.validarToken(token);
        
        if (tokenOpt.isPresent()) {
            PasswordResetToken resetToken = tokenOpt.get();
            response.put("valid", true);
            response.put("email", resetToken.getUsuario().getCorreo());
            response.put("nombre", resetToken.getUsuario().getNombre());
            response.put("expiraEn", resetToken.getFechaExpiracion().toString());
            return ResponseEntity.ok(response);
        } else {
            response.put("valid", false);
            response.put("message", "Token inválido o expirado");
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * POST /api/password-reset/change
     * Cambia la contraseña usando el token
     * Body: { "token": "abc123", "newPassword": "nuevaclave123" }
     */
    @PostMapping("/change")
    public ResponseEntity<Map<String, Object>> cambiarPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        
        if (token == null || token.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Token requerido");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "La nueva contraseña es requerida");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (newPassword.length() < 6) {
            response.put("success", false);
            response.put("message", "La contraseña debe tener al menos 6 caracteres");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean cambiado = resetService.cambiarPassword(token, newPassword);
        
        if (cambiado) {
            response.put("success", true);
            response.put("message", "Contraseña actualizada correctamente. Ya puedes iniciar sesión.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Token inválido o expirado");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
