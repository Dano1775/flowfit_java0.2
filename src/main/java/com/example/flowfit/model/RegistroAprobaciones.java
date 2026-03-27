package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_aprobaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroAprobaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Usuario admin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Accion accion;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "comentarios", columnDefinition = "TEXT")
    private String motivo;

    public enum Accion {
        Aprobado, Rechazado, Editado
    }

    // Alias methods for compatibility
    public void setAdministrador(Usuario administrador) {
        this.admin = administrador;
    }

    public Usuario getAdministrador() {
        return this.admin;
    }

    public void setFechaAccion(LocalDateTime fechaAccion) {
        this.fecha = fechaAccion;
    }

    public LocalDateTime getFechaAccion() {
        return this.fecha;
    }

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

    public Usuario getAdmin() {
        return admin;
    }

    public void setAdmin(Usuario admin) {
        this.admin = admin;
    }

    public Accion getAccion() {
        return accion;
    }

    public void setAccion(Accion accion) {
        this.accion = accion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}