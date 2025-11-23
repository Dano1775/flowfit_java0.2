package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "asignacion_entrenador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionEntrenador {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", nullable = false)
    private Usuario entrenador;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoAsignacion estado = EstadoAsignacion.PENDIENTE;
    
    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;
    
    @Column(name = "fecha_aceptacion")
    private LocalDateTime fechaAceptacion;
    
    @Column(name = "mensaje_solicitud", length = 500)
    private String mensajeSolicitud;
    
    @Column(name = "mensaje_respuesta", length = 500)
    private String mensajeRespuesta;
    
    @PrePersist
    protected void onCreate() {
        fechaSolicitud = LocalDateTime.now();
    }
    
    public enum EstadoAsignacion {
        PENDIENTE, ACEPTADA, RECHAZADA
    }
}