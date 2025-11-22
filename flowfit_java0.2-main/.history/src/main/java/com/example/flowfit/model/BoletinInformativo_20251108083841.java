package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad para boletines informativos y envíos masivos de correo
 */
@Entity
@Table(name = "boletin_informativo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoletinInformativo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String asunto;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_destinatario", nullable = false)
    private TipoDestinatario tipoDestinatario;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_envio")
    private EstadoEnvio estadoEnvio = EstadoEnvio.PENDIENTE;
    
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;
    
    @Column(name = "total_destinatarios")
    private Integer totalDestinatarios = 0;
    
    @Column(name = "enviados_exitosos")
    private Integer enviadosExitosos = 0;
    
    @Column(name = "enviados_fallidos")
    private Integer enviadosFallidos = 0;
    
    @Column(name = "creado_por", nullable = false)
    private String creadoPor;
    
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        actualizadoEn = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
    
    /**
     * Enum para tipos de destinatarios
     */
    public enum TipoDestinatario {
        TODOS("Todos los usuarios"),
        USUARIOS("Solo usuarios regulares"),
        ENTRENADORES("Solo entrenadores"),
        NUTRICIONISTAS("Solo nutricionistas"),
        ADMINISTRADORES("Solo administradores"),
        USUARIOS_ACTIVOS("Usuarios activos (estado A)"),
        USUARIOS_INACTIVOS("Usuarios inactivos (estado I)");
        
        private final String descripcion;
        
        TipoDestinatario(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    /**
     * Enum para estados de envío
     */
    public enum EstadoEnvio {
        PENDIENTE("Pendiente de envío"),
        ENVIANDO("Enviando..."),
        COMPLETADO("Completado exitosamente"),
        FALLIDO("Falló el envío");
        
        private final String descripcion;
        
        EstadoEnvio(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
}
