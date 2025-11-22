package com.example.flowfit.dto;

import java.time.LocalDate;

/**
 * DTO para mostrar detalles de rutina en API JSON
 * Evita problemas de serialización con proxies de Hibernate
 */
public class RutinaDetalleDto {
    private Integer id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaCreacion;
    private String entrenadorNombre;
    
    // Constructores
    public RutinaDetalleDto() {}
    
    public RutinaDetalleDto(Integer id, String nombre, String descripcion, 
                           LocalDate fechaCreacion, String entrenadorNombre) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.entrenadorNombre = entrenadorNombre;
    }
    
    // Getters y Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public String getEntrenadorNombre() {
        return entrenadorNombre;
    }
    
    public void setEntrenadorNombre(String entrenadorNombre) {
        this.entrenadorNombre = entrenadorNombre;
    }
}