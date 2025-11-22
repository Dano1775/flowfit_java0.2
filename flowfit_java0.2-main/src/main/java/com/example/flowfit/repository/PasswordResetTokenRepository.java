package com.example.flowfit.repository;

import com.example.flowfit.model.PasswordResetToken;
import com.example.flowfit.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    /**
     * Busca un token específico
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * Busca todos los tokens de un usuario
     */
    Optional<PasswordResetToken> findByUsuario(Usuario usuario);
    
    /**
     * Elimina tokens expirados (para limpieza automática)
     */
    void deleteByFechaExpiracionBefore(LocalDateTime fechaLimite);
    
    /**
     * Elimina todos los tokens de un usuario (al resetear contraseña exitosamente)
     */
    void deleteByUsuario(Usuario usuario);
}
