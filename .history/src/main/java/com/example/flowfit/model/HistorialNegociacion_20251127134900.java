package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_negociacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialNegociacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "contratacion_id", nullable = false)
    private Long contratacionId;
    
    @Column(nullable = false)
    private Integer version;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "propuesto_por", nullable = false)
    private PropuestoPor propuestoPor;
    
    @Column(name = "precio_propuesto", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPropuesto;
    
    @Column(name = "duracion_propuesta", nullable = false)
    private Integer duracionPropuesta;
    
    @Column(name = "servicios_propuestos", columnDefinition = "JSON")
    private String serviciosPropuestos;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_propuesta", nullable = false)
    private EstadoPropuesta estadoPropuesta = EstadoPropuesta.PENDIENTE;
    
    @Column(columnDefinition = "TEXT")
    private String mensaje;
    
    @Column(name = "fecha_propuesta", nullable = false)
    private LocalDateTime fechaPropuesta = LocalDateTime.now();
    
    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contratacion_id", insertable = false, updatable = false)
    private ContratacionEntrenador contratacion;
    
    public enum PropuestoPor {
        USUARIO,
        ENTRENADOR
    }
    
    public enum EstadoPropuesta {
        PENDIENTE,
        ACEPTADA,
        RECHAZADA,
        CONTRAOFERTA
    }
}
