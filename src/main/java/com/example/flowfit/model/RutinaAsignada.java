package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

@Entity
@Table(name = "rutina_asignada")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutinaAsignada {

    public enum EstadoRutina {
        BORRADOR, ACTIVA, COMPLETADA, PAUSADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "rutina_id", nullable = false)
    private Integer rutinaId;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "fecha_asignacion")
    private LocalDate fechaAsignacion;

    @Column(name = "fecha_completada")
    private LocalDate fechaCompletada;

    // Campos adicionales para seguimiento
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoRutina estado = EstadoRutina.ACTIVA;

    @Column(name = "progreso")
    private Integer progreso = 0; // Porcentaje de progreso

    @Transient
    private LocalDate ultimaActividad;

    @Transient
    private Integer vecesCompletada = 0;

    // Relaciones
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rutina_id", insertable = false, updatable = false)
    @JsonIgnore
    @ToString.Exclude
    private Rutina rutina;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    @ToString.Exclude
    private Usuario usuario;

    // Métodos de utilidad
    public boolean estaActiva() {
        return "ACTIVA".equals(estado);
    }

    public boolean estaCompletada() {
        return EstadoRutina.COMPLETADA.equals(estado);
    }

    public void marcarComoCompletada() {
        this.estado = EstadoRutina.COMPLETADA;
        this.progreso = 100;
        this.ultimaActividad = LocalDate.now();
        this.fechaCompletada = LocalDate.now();
        this.vecesCompletada++;
    }

    public void actualizarProgreso(int nuevoProgreso) {
        this.progreso = Math.max(0, Math.min(100, nuevoProgreso));
        this.ultimaActividad = LocalDate.now();

        if (this.progreso >= 100) {
            marcarComoCompletada();
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRutinaId() {
        return rutinaId;
    }

    public void setRutinaId(Integer rutinaId) {
        this.rutinaId = rutinaId;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDate fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public LocalDate getFechaCompletada() {
        return fechaCompletada;
    }

    public void setFechaCompletada(LocalDate fechaCompletada) {
        this.fechaCompletada = fechaCompletada;
    }

    public EstadoRutina getEstado() {
        return estado;
    }

    public void setEstado(EstadoRutina estado) {
        this.estado = estado;
    }

    public Integer getProgreso() {
        return progreso;
    }

    public void setProgreso(Integer progreso) {
        this.progreso = progreso;
    }

    public LocalDate getUltimaActividad() {
        return ultimaActividad;
    }

    public void setUltimaActividad(LocalDate ultimaActividad) {
        this.ultimaActividad = ultimaActividad;
    }

    public Integer getVecesCompletada() {
        return vecesCompletada;
    }

    public void setVecesCompletada(Integer vecesCompletada) {
        this.vecesCompletada = vecesCompletada;
    }

    public Rutina getRutina() {
        return rutina;
    }

    public void setRutina(Rutina rutina) {
        this.rutina = rutina;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}