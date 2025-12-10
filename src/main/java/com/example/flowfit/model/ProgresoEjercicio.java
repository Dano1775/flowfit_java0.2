package com.example.flowfit.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "progreso_ejercicio")
public class ProgresoEjercicio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "rutina_asignada_id", nullable = false)
    private RutinaAsignada rutinaAsignada;
    
    @ManyToOne
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private EjercicioCatalogo ejercicio;
    
    @Column(name = "fecha")
    private LocalDate fecha;
    
    @Column(name = "series_completadas")
    private Integer seriesCompletadas = 0;
    
    @Column(name = "repeticiones_realizadas")
    private Integer repeticionesRealizadas = 0;
    
    @Column(name = "peso_utilizado")
    private Double pesoUtilizado;
    
    @Column(name = "tiempo_segundos")
    private Integer tiempoSegundos;
    
    @Column(name = "comentarios", columnDefinition = "TEXT")
    private String comentarios;
    
    // Constructors
    public ProgresoEjercicio() {
        this.fecha = LocalDate.now();
    }
    
    public ProgresoEjercicio(Usuario usuario, RutinaAsignada rutinaAsignada, EjercicioCatalogo ejercicio) {
        this();
        this.usuario = usuario;
        this.rutinaAsignada = rutinaAsignada;
        this.ejercicio = ejercicio;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public RutinaAsignada getRutinaAsignada() {
        return rutinaAsignada;
    }
    
    public void setRutinaAsignada(RutinaAsignada rutinaAsignada) {
        this.rutinaAsignada = rutinaAsignada;
    }
    
    public EjercicioCatalogo getEjercicio() {
        return ejercicio;
    }
    
    public void setEjercicio(EjercicioCatalogo ejercicio) {
        this.ejercicio = ejercicio;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public Integer getSeriesCompletadas() {
        return seriesCompletadas;
    }
    
    public void setSeriesCompletadas(Integer seriesCompletadas) {
        this.seriesCompletadas = seriesCompletadas;
    }
    
    public Integer getRepeticionesRealizadas() {
        return repeticionesRealizadas;
    }
    
    public void setRepeticionesRealizadas(Integer repeticionesRealizadas) {
        this.repeticionesRealizadas = repeticionesRealizadas;
    }
    
    public Double getPesoUtilizado() {
        return pesoUtilizado;
    }
    
    public void setPesoUtilizado(Double pesoUtilizado) {
        this.pesoUtilizado = pesoUtilizado;
    }
    
    public Integer getTiempoSegundos() {
        return tiempoSegundos;
    }
    
    public void setTiempoSegundos(Integer tiempoSegundos) {
        this.tiempoSegundos = tiempoSegundos;
    }
    
    public String getComentarios() {
        return comentarios;
    }
    
    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
}
