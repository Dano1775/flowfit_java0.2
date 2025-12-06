package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "plan_entrenador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanEntrenador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "entrenador_id", nullable = false)
    private Integer entrenadorId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_mensual", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMensual;

    @Column(name = "rango_precio_min", precision = 10, scale = 2)
    private BigDecimal rangoPrecioMin;

    @Column(name = "rango_precio_max", precision = 10, scale = 2)
    private BigDecimal rangoPrecioMax;

    @Column(name = "duracion_dias")
    private Integer duracionDias = 30;

    @Column(name = "rutinas_mes")
    private Integer rutinasMes;

    @Column(name = "seguimiento_semanal")
    private Boolean seguimientoSemanal = false;

    @Column(name = "chat_directo")
    private Boolean chatDirecto = true;

    @Column(name = "videollamadas_mes")
    private Integer videollamadasMes = 0;

    @Column(name = "plan_nutricional")
    private Boolean planNutricional = false;

    @Column(name = "es_publico")
    private Boolean esPublico = true;

    @Column(name = "permite_personalizacion")
    private Boolean permitePersonalizacion = true;

    @Column(nullable = false)
    private Boolean destacado = false;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    // Campo transitorio para mostrar en la vista (no se guarda en BD)
    @Transient
    private Integer clientesActivos = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", insertable = false, updatable = false)
    private Usuario entrenador;

    @OneToMany(mappedBy = "planBaseId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContratacionEntrenador> contrataciones;

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
