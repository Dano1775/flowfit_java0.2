package com.example.flowfit.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "rutina_ejercicio_programado")
public class RutinaEjercicioProgramado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "rutina_asignada_id", nullable = false)
    private Integer rutinaAsignadaId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "ejercicio_id", nullable = false)
    private Integer ejercicioId;

    @Column(name = "orden", nullable = false)
    private Integer orden = 1;

    public RutinaEjercicioProgramado() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRutinaAsignadaId() {
        return rutinaAsignadaId;
    }

    public void setRutinaAsignadaId(Integer rutinaAsignadaId) {
        this.rutinaAsignadaId = rutinaAsignadaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
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
}
