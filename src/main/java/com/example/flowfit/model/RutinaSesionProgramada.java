package com.example.flowfit.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "rutina_sesion_programada", uniqueConstraints = {
        @UniqueConstraint(name = "uq_rutina_asignada_fecha", columnNames = { "rutina_asignada_id", "fecha" })
}, indexes = {
        @Index(name = "idx_fecha", columnList = "fecha"),
        @Index(name = "idx_rutina_asignada_fecha", columnList = "rutina_asignada_id,fecha")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutinaSesionProgramada {

    public enum EstadoSesion {
        PROGRAMADA,
        REALIZADA,
        CANCELADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_asignada_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private RutinaAsignada rutinaAsignada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_dia_id")
    @JsonIgnore
    @ToString.Exclude
    private RutinaDia rutinaDia;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoSesion estado = EstadoSesion.PROGRAMADA;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;
}
