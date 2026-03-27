package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "ejercicio_catalogo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EjercicioCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 255)
    private String imagen;

    @ManyToOne
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    public Integer getCreadorId() {
        return creadoPor != null ? creadoPor.getId() : null;
    }

    public void setCreadorId(Integer creadorId) {
        if (creadorId != null) {
            Usuario usuario = new Usuario();
            usuario.setId(creadorId);
            this.creadoPor = usuario;
        } else {
            this.creadoPor = null;
        }
    }

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

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Usuario getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(Usuario creadoPor) {
        this.creadoPor = creadoPor;
    }
}