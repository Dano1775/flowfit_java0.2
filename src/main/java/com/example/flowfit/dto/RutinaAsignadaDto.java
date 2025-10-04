package com.example.flowfit.dto;

import java.time.LocalDate;

/**
 * DTO para mostrar rutina asignada en API JSON
 * Evita problemas de serialización con proxies de Hibernate
 */
public class RutinaAsignadaDto {
    private Integer id;
    private Integer usuarioId;
    private Integer rutinaId;
    private LocalDate fechaAsignacion;
    private LocalDate fechaCompletada;
    private Integer progreso;
    private String estado;
    
    // Constructores
    public RutinaAsignadaDto() {}
    
    public RutinaAsignadaDto(Integer id, Integer usuarioId, Integer rutinaId, 
                            LocalDate fechaAsignacion, LocalDate fechaCompletada, 
                            Integer progreso, String estado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.rutinaId = rutinaId;
        this.fechaAsignacion = fechaAsignacion;
        this.fechaCompletada = fechaCompletada;
        this.progreso = progreso;
        this.estado = estado;
    }
    
    // Getters y Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public Integer getRutinaId() {
        return rutinaId;
    }
    
    public void setRutinaId(Integer rutinaId) {
        this.rutinaId = rutinaId;
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
    
    public Integer getProgreso() {
        return progreso;
    }
    
    public void setProgreso(Integer progreso) {
        this.progreso = progreso;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
}