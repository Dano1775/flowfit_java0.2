package com.example.flowfit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "rutina_dia", uniqueConstraints = {
        @UniqueConstraint(name = "uq_rutina_dia_orden", columnNames = { "rutina_id", "orden" })
}, indexes = {
        @Index(name = "idx_rutina_dia_rutina", columnList = "rutina_id"),
        @Index(name = "idx_rutina_dia_rutina_orden", columnList = "rutina_id,orden")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutinaDia {

    public enum TipoDia {
        ENTRENAMIENTO,
        DESCANSO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "rutina_id", nullable = false)
    private Integer rutinaId;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoDia tipo = TipoDia.ENTRENAMIENTO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_id", insertable = false, updatable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Rutina rutina;
}
