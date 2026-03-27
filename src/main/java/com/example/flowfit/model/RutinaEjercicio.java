package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;

@Entity
@Table(name = "rutina_ejercicio")
@IdClass(RutinaEjercicio.RutinaEjercicioId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutinaEjercicio {

    @Id
    @Column(name = "rutina_id", nullable = false)
    private Integer rutinaId;

    @Id
    @Column(name = "ejercicio_id", nullable = false)
    private Integer ejercicioId;

    @Column(name = "orden")
    private Integer orden;

    @Column(name = "dia_orden")
    private Integer diaOrden;

    @Column(name = "series")
    private Integer series;

    @Column(name = "repeticiones")
    private Integer repeticiones;

    @Column(name = "duracion_segundos")
    private Integer duracionSegundos;

    @Column(name = "descanso_segundos")
    private Integer descansoSegundos;

    @Column(name = "peso_kg")
    private Double pesoKg;

    @Column(name = "notas", length = 500)
    private String notas;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_id", insertable = false, updatable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Rutina rutina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ejercicio_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EjercicioCatalogo ejercicioCatalogo;

    // Métodos de utilidad
    public String getDescripcionCompleta() {
        StringBuilder desc = new StringBuilder();

        if (series != null && repeticiones != null) {
            desc.append(series).append(" x ").append(repeticiones);
        }

        if (duracionSegundos != null && duracionSegundos > 0) {
            desc.append(" (").append(duracionSegundos).append("s)");
        }

        if (pesoKg != null && pesoKg > 0) {
            desc.append(" - ").append(pesoKg).append(" kg");
        }

        return desc.toString();
    }

    public Integer getDuracionTotalSegundos() {
        int total = 0;

        if (duracionSegundos != null) {
            total += duracionSegundos;
        } else if (series != null && repeticiones != null) {
            // Estimación: 2 segundos por repetición
            total += series * repeticiones * 2;
        }

        if (descansoSegundos != null && series != null && series > 1) {
            total += descansoSegundos * (series - 1);
        }

        return total;
    }

    // Clase para la clave primaria compuesta
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RutinaEjercicioId implements Serializable {
        private Integer rutinaId;
        private Integer ejercicioId;
    }

    public Integer getRutinaId() {
        return rutinaId;
    }

    public void setRutinaId(Integer rutinaId) {
        this.rutinaId = rutinaId;
    }

    public Integer getEjercicioId() {
        return ejercicioId;
    }

    public void setEjercicioId(Integer ejercicioId) {
        this.ejercicioId = ejercicioId;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Integer getDiaOrden() {
        return diaOrden;
    }

    public void setDiaOrden(Integer diaOrden) {
        this.diaOrden = diaOrden;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public Integer getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(Integer repeticiones) {
        this.repeticiones = repeticiones;
    }

    public Integer getDuracionSegundos() {
        return duracionSegundos;
    }

    public void setDuracionSegundos(Integer duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }

    public Integer getDescansoSegundos() {
        return descansoSegundos;
    }

    public void setDescansoSegundos(Integer descansoSegundos) {
        this.descansoSegundos = descansoSegundos;
    }

    public Double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(Double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Rutina getRutina() {
        return rutina;
    }

    public void setRutina(Rutina rutina) {
        this.rutina = rutina;
    }

    public EjercicioCatalogo getEjercicioCatalogo() {
        return ejercicioCatalogo;
    }

    public void setEjercicioCatalogo(EjercicioCatalogo ejercicioCatalogo) {
        this.ejercicioCatalogo = ejercicioCatalogo;
    }
}