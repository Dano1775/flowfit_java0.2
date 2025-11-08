package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Token temporal para recuperación de contraseña
 * Se genera cuando el usuario solicita restablecer su contraseña
 * Expira después de un tiempo definido (15 minutos por defecto)
 */
@Entity
@Table(name = "password_reset_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String token;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "usado", nullable = false)
    private boolean usado = false;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        // Token válido por 15 minutos
        fechaExpiracion = LocalDateTime.now().plusMinutes(15);
    }
    
    /**
     * Verifica si el token ya expiró
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }
    
    /**
     * Verifica si el token es válido (no usado y no expirado)
     */
    public boolean isValid() {
        return !usado && !isExpired();
    }
}
